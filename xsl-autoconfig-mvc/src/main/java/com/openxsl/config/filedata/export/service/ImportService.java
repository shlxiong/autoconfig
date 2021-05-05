package com.openxsl.config.filedata.export.service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Service;

import com.openxsl.config.dal.ExcelUtils;
import com.openxsl.config.dal.jdbc.BaseEntity;
import com.openxsl.config.filedata.export.dao.ImportConfigDao;
import com.openxsl.config.filedata.export.dao.ImportLogDao;
import com.openxsl.config.filedata.export.dao.ImportMappingDao;
import com.openxsl.config.filedata.export.entity.ImportConfig;
import com.openxsl.config.filedata.export.entity.ImportLog;
import com.openxsl.config.filedata.export.entity.ImportMapping;
import com.openxsl.config.util.StringUtils;

@ConditionalOnClass(JdbcTemplate.class)
@Service
public class ImportService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private ImportMappingDao mappingDao;
	@Autowired
	private ImportConfigDao configDao;
	@Autowired
	private ImportLogDao importDao;
	
	public void importFile(ImportLog importLog) {
		importLog.setImportTime(new Date());
		String importName = importLog.getImportName();
		String scenicCode = importLog.getScenicCode();
		ImportConfig importConfig = configDao.getImportConfig(importName, scenicCode);
		String tableName = importConfig.getTableName();
		boolean skipFirst = importConfig.isFirstCaption();
		List<ImportMapping> mappings = mappingDao.getMappings(importName, scenicCode);
		String sql = this.getSql(tableName, mappings);
		int succs = 0, fails = 0;
		try {
			for (Map<String,?> rowData : ExcelUtils.readFile(importLog.getSourceFile(), skipFirst)) {
				Object[] args = new Object[mappings.size()];
				int i = 0;
				for (ImportMapping mapping : mappings) {
					args[i++] = this.getValue(rowData, mapping);
				}
				if (this.executeInsert(sql, args) > 0) {
					succs ++;
				} else {
					fails ++;
					importLog.setFailRecord((String)rowData.get("A"));  //第一列为Key
				}
			}
			importLog.setSuccessNum(succs);
			importLog.setFailNum(fails);
			importLog.setTotalNum(succs+fails);
			importDao.insert(importLog);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 根据表名生成初始的Mapping
	 * @param tableName
	 * @throws SQLException 
	 */
	public Collection<ImportMapping> generateMappings(String tableName) {
		Map<String,ImportMapping> columnMap = new LinkedHashMap<String,ImportMapping>();
		String sql = new StringBuilder("SELECT * FROM ").append(tableName)
				.append(" WHERE 1<>1").toString();
		try {
			ResultSet rs = jdbcTemplate.getDataSource().getConnection()
								.prepareStatement(sql).executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			for (int i=2,cnt=rsmd.getColumnCount(); i<=cnt; i++) { //1:Id，从第二个开始
				ImportMapping mapping = new ImportMapping();
				mapping.setColumnName(rsmd.getColumnName(i));
				mapping.setDataType(rsmd.getColumnClassName(i));  //JdbcUtils.getTypeName(rsmd.getColumnType(i));
				mapping.setMaxLen(rsmd.getPrecision(i));
				columnMap.put(mapping.getColumnName(), mapping);
			}
			columnMap.remove(StringUtils.camelToSplitName(BaseEntity.CREATE_BY_FIELD, "_"));
			columnMap.remove(StringUtils.camelToSplitName(BaseEntity.CREATE_TIME_FIELD, "_"));
			columnMap.remove(StringUtils.camelToSplitName(BaseEntity.MODIFY_BY_FIELD, "_"));
			columnMap.remove(StringUtils.camelToSplitName(BaseEntity.MODIFY_TIME_FIELD, "_"));
			columnMap.remove(StringUtils.camelToSplitName(BaseEntity.DELETED_FIELD, "_"));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		return columnMap.values();
	}
	
	/**
	 * 通过名称获取Mapping
	 * @param name
	 */
	public List<ImportMapping> getMappings(String name, String scenicCode){
		return mappingDao.getMappings(name, scenicCode);
	}
	
	public ImportConfig getImportConfig(String name, String scenicCode) {
		return configDao.getImportConfig(name, scenicCode);
	}
	
	/**
	 * 保存文件-表的映射关系
	 * @param name
	 */
	public void saveMappings(String importName, ImportMapping[] importMappings, String scenicCode) {
		if (importMappings==null || importMappings.length < 1) {
			return;
		}
		//删除原来的
		mappingDao.deleteByConfigName(importName, scenicCode);
		for (ImportMapping mapping : importMappings) {
			if (mapping.getConfigName() == null) {
				mapping.setConfigName(importName);
			}
			mappingDao.insert(mapping);
		}
	}
//	@see ImportConfigService
//	public void saveImportConfig(ImportConfig config) {
//		int cnt = configDao.update(config);
//		if (cnt < 1) {
//			configDao.insert(config);
//		}
//	}
	
	private String getSql(String tableName, List<ImportMapping> mappings) {
		StringBuilder buffer = new StringBuilder("INSERT INTO ").append(tableName);
		StringBuilder fieldBuf = new StringBuilder("(");
		StringBuilder valueBuf = new StringBuilder(") VALUES (");
		for (ImportMapping mapping : mappings) {
			fieldBuf.append(mapping.getColumnName()).append(", ");
			valueBuf.append("?, ");
		}
		String createBy = StringUtils.camelToSplitName(BaseEntity.CREATE_TIME_FIELD, "_");
		String modifyBy = StringUtils.camelToSplitName(BaseEntity.MODIFY_TIME_FIELD, "_");
		fieldBuf.append(createBy).append(", ").append(modifyBy);
		valueBuf.append("now(), now())");
		buffer.append(fieldBuf).append(valueBuf);
//			.append(valueBuf.substring(0, valueBuf.length()-2)).append(')');
		return buffer.toString();
	}
	private Object getValue(Map<String,?> rowData, ImportMapping mapping) {
		Object value = rowData.get(mapping.getExcelColumnNo());
		String reference = mapping.getReference();  //table.column1 on column2
		if (!StringUtils.isEmpty(reference)) {
			String[] temps = reference.split("\\.|( on )");
			String sql = String.format("SELECT %s FROM %s WHERE %s = ?", temps[1],temps[0],temps[2]);
			Map<String,Object> map = jdbcTemplate.queryForMap(sql, value);
			String name2 = temps[1].toUpperCase();
			value = map.getOrDefault(temps[1], map.getOrDefault(name2, value));
		}
		return value;
	}
	private int executeInsert(String sql, Object[] args) {
		try {
			return jdbcTemplate.execute(sql, new PreparedStatementCallback<Integer>() {
				@Override
				public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
					int i = 0;
					for (Object arg : args) {
						ps.setObject(++i, arg);
					}
					return ps.executeUpdate();
				}
			});
		} catch (Exception e) {
			logger.error("", e);
			return 0;
		}
	}
//	private int batchInsert(String sql, List<ImportMapping> mappings) {
//		int batchSize = 100;
//		int dataSize = 0;
//		List<Object[]> batchArgs = new ArrayList<Object[]>(batchSize);
//		for (Map<String,?> rowData : ExcelUtils.readFile(importLog.getSourceFile(), 0)) {
//			Object[] args = null;
//			for (ImportMapping mapping : mappings) {
//				Object value = rowData.get(mapping.getExcelColumnNo());
//				String reference = mapping.getReference();
//				if (StringUtils.isEmpty(reference)) {
//					
//				}
//			}
//			batchArgs.add(args);
//			if (batchSize == batchArgs.size() || dataSize == batchArgs.size()) {
//				int[] rows = jdbcTemplate.batchUpdate(sql, batchArgs);
//				batchArgs.clear();
//			}
//		}
//	}
	
}
