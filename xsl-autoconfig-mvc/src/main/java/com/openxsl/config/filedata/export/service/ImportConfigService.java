package com.openxsl.config.filedata.export.service;

import org.springframework.stereotype.Service;

import com.openxsl.config.dal.jdbc.BaseService;
import com.openxsl.config.filedata.export.dao.ImportConfigDao;
import com.openxsl.config.filedata.export.entity.ImportConfig;

@Service
public class ImportConfigService extends BaseService<ImportConfigDao, ImportConfig, Integer> {

}
