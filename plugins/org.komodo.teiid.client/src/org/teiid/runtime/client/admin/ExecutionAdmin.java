/*************************************************************************************
 * JBoss, Home of Professional Open Source.
* See the COPYRIGHT.txt file distributed with this work for information
* regarding copyright ownership. Some portions may be licensed
* to Red Hat, Inc. under one or more contributor license agreements.
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
* 02110-1301 USA.
 ************************************************************************************/
package org.teiid.runtime.client.admin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.komodo.spi.annotation.AnnotationUtils;
import org.komodo.spi.annotation.Removed;
import org.komodo.spi.outcome.IOutcome;
import org.komodo.spi.outcome.OutcomeFactory;
import org.komodo.spi.runtime.EventManager;
import org.komodo.spi.runtime.ExecutionConfigurationEvent;
import org.komodo.spi.runtime.IDataSourceDriver;
import org.komodo.spi.runtime.IExecutionAdmin;
import org.komodo.spi.runtime.ITeiidConnectionInfo;
import org.komodo.spi.runtime.ITeiidDataSource;
import org.komodo.spi.runtime.ITeiidInstance;
import org.komodo.spi.runtime.ITeiidJdbcInfo;
import org.komodo.spi.runtime.ITeiidTranslator;
import org.komodo.spi.runtime.ITeiidTranslator.TranslatorPropertyType;
import org.komodo.spi.runtime.ITeiidVdb;
import org.komodo.spi.runtime.TeiidExecutionException;
import org.komodo.spi.runtime.TeiidPropertyDefinition;
import org.komodo.spi.runtime.version.ITeiidVersion;
import org.komodo.spi.runtime.version.TeiidVersion.Version;
import org.komodo.utils.KLog;
import org.teiid.adminapi.Admin;
import org.teiid.adminapi.PropertyDefinition;
import org.teiid.adminapi.Translator;
import org.teiid.adminapi.VDB;
import org.teiid.core.util.ArgCheck;
import org.teiid.jdbc.TeiidDriver;
import org.teiid.runtime.client.Messages;



/**
 *
 *
 *
 */
public class ExecutionAdmin implements IExecutionAdmin {

    private static String DYNAMIC_VDB_SUFFIX = "-vdb.xml"; //$NON-NLS-1$
    private static int VDB_LOADING_TIMEOUT_SEC = 300;

    private final Admin admin;
    protected Map<String, ITeiidTranslator> translatorByNameMap;
    protected Collection<String> dataSourceNames;
    protected Map<String, ITeiidDataSource> dataSourceByNameMap;
    protected Set<String> dataSourceTypeNames;
    private final EventManager eventManager;
    private final ITeiidInstance teiidInstance;
    private final AdminSpec adminSpec;
    private Map<String, ITeiidVdb> teiidVdbs;
    private final ModelConnectionMatcher connectionMatcher;

    private boolean loaded = false;

    /**
     * Constructor used for testing purposes only. 
     * 
     * @param admin the associated Teiid Admin API (never <code>null</code>)
     * @param teiidInstance the teiid instancenstance this admin belongs to (never <code>null</code>)
     * @throws Exception if there is a problem connecting the teiid instancenstance
     */
    ExecutionAdmin(Admin admin, ITeiidInstance teiidInstance) throws Exception {
        ArgCheck.isNotNull(admin, "admin"); //$NON-NLS-1$
        ArgCheck.isNotNull(teiidInstance, "server"); //$NON-NLS-1$
        
        this.admin = admin;
        this.teiidInstance = teiidInstance;
        this.adminSpec = AdminSpec.getInstance(teiidInstance.getVersion());
        this.eventManager = teiidInstance.getEventManager();
        this.connectionMatcher = new ModelConnectionMatcher();
        
        init();
    }
    
    /**
     * Default Constructor 
     * 
     * @param teiidInstance the teiid instance this admin belongs to (never <code>null</code>)
     * 
     * @throws Exception if there is a problem connecting the teiid instance
     */
    public ExecutionAdmin(ITeiidInstance teiidInstance) throws Exception {
        ArgCheck.isNotNull(teiidInstance, "server"); //$NON-NLS-1$

        this.adminSpec = AdminSpec.getInstance(teiidInstance.getVersion());

        this.admin = adminSpec.createAdmin(teiidInstance);
        ArgCheck.isNotNull(admin, "admin"); //$NON-NLS-1$

        this.teiidInstance = teiidInstance;
        this.eventManager = teiidInstance.getEventManager();
        this.connectionMatcher = new ModelConnectionMatcher();

        init();
    }

    private boolean isLessThanTeiidEight() {
        ITeiidVersion minVersion = teiidInstance.getVersion().getMinimumVersion();
        return minVersion.isLessThan(Version.TEIID_8_0.get());
    }
    
    private boolean isLessThanTeiidEightSeven() {
        ITeiidVersion minVersion = teiidInstance.getVersion().getMinimumVersion();
        return minVersion.isLessThan(Version.TEIID_8_7.get());
    }

    @Override
    public boolean dataSourceExists( String name ) {
        // Check if exists, return false
        if (this.dataSourceNames.contains(name)) {
            return true;
        }

        return false;
    }

    @Override
    public void deleteDataSource( String dsName ) throws Exception {
        // Check if exists, return false
        if (this.dataSourceNames.contains(dsName)) {
            this.admin.deleteDataSource(dsName);

            if (!this.admin.getDataSourceNames().contains(dsName)) {
                this.dataSourceNames.remove(dsName);
                ITeiidDataSource tds = this.dataSourceByNameMap.get(dsName);

                if (tds != null) {
                    this.dataSourceByNameMap.remove(dsName);
                    this.eventManager.notifyListeners(ExecutionConfigurationEvent.createRemoveDataSourceEvent(tds));

                }
            }
        }
    }
    
    @Override
    public void deployDynamicVdb( String deploymentName, InputStream inStream ) throws Exception {
        ArgCheck.isNotNull(deploymentName, "deploymentName"); //$NON-NLS-1$
        ArgCheck.isNotNull(inStream, "inStream"); //$NON-NLS-1$

        // Check dynamic VDB deployment name
        if(!deploymentName.endsWith(DYNAMIC_VDB_SUFFIX)) {
            throw new Exception(Messages.getString(Messages.ExecutionAdmin.dynamicVdbInvalidName, deploymentName));
        }
        
        // Get VDB name
        String vdbName = deploymentName.substring(0, deploymentName.indexOf(DYNAMIC_VDB_SUFFIX));

        // For Teiid Version less than 8.7, do explicit undeploy (TEIID-2873)
    	if(isLessThanTeiidEightSeven()) {
    		undeployDynamicVdb(vdbName);
    	}
    	
        // Deploy the VDB
        // TODO: Dont assume vdbVersion
        doDeployVdb(deploymentName,vdbName,1,inStream);
    }
    
    private void doDeployVdb(String deploymentName, String vdbName, int vdbVersion, InputStream inStream) throws Exception {
        adminSpec.deploy(admin, deploymentName, inStream);
        // Give a 0.5 sec pause for the VDB to finish loading metadata.
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }   
                      
        // Refresh VDBs list
        refreshVDBs();

        // TODO should get version from vdbFile
        VDB vdb = admin.getVDB(vdbName, vdbVersion);

        Thread refreshThread = new RefreshThread(vdb);
        refreshThread.start();
    }

    @Override
    public String getSchema(String vdbName, int vdbVersion, String modelName) throws Exception {
        if (isLessThanTeiidEight()) {
            // Limited schema support in 77x, just return empty string here
            return ""; //$NON-NLS-1$
        }

        return admin.getSchema(vdbName, vdbVersion, modelName, null, null);
    }
        
    @Override
    public void disconnect() {
    	// 
    	this.admin.close();
        this.translatorByNameMap = new HashMap<String, ITeiidTranslator>();
        this.dataSourceNames = new ArrayList<String>();
        this.dataSourceByNameMap = new HashMap<String, ITeiidDataSource>();
        this.dataSourceTypeNames = new HashSet<String>();
        this.teiidVdbs = new HashMap<String, ITeiidVdb>();
    }

    @Override
    public ITeiidDataSource getDataSource(String name) {
        return this.dataSourceByNameMap.get(name);
    }
    
    @Override
	public Collection<ITeiidDataSource> getDataSources() {
        return this.dataSourceByNameMap.values();
    }

    @Override
	public Set<String> getDataSourceTypeNames() {
        return this.dataSourceTypeNames;
    }

    /**
     * @return the event manager (never <code>null</code>)
     */
    public EventManager getEventManager() {
        return this.eventManager;
    }

    @Override
    public ITeiidDataSource getOrCreateDataSource( String displayName,
                                                  String dsName,
                                                  String typeName,
                                                  Properties properties ) throws Exception {
        ArgCheck.isNotEmpty(displayName, "displayName"); //$NON-NLS-1$
        ArgCheck.isNotEmpty(dsName, "dsName"); //$NON-NLS-1$
        ArgCheck.isNotEmpty(typeName, "typeName"); //$NON-NLS-1$
        ArgCheck.isNotEmpty(properties, "properties"); //$NON-NLS-1$

        // Check if exists, return false
        if (dataSourceExists(dsName)) {
            ITeiidDataSource tds = this.dataSourceByNameMap.get(dsName);
            if (tds != null) {
                return tds;
            }
        }

        // For JDBC types, find the matching installed driver.  This is done currently by matching
        // the profile driver classname to the installed driver classname
        String connProfileDriverClass = properties.getProperty("driver-class");  //$NON-NLS-1$
        if("connector-jdbc".equals(typeName)) {  //$NON-NLS-1$
            // List of driver jars on the connection profile
            String jarList = properties.getProperty("jarList");  //$NON-NLS-1$
            
            // Get first driver name with the driver class that matches the connection profile
            String dsNameMatch = getDSMatchForDriverClass(connProfileDriverClass);
            
            // If a matching datasource was found, set typename
            if(dsNameMatch!=null) {
                typeName=dsNameMatch;
            // No matching datasource, attempt to deploy the driver if jarList is populated.
            } else if(jarList!=null && jarList.trim().length()>0) {
                // Try to deploy the jars
                deployJars(this.admin,jarList);
                
                refresh();
                
                // Retry the name match after deployment.
                dsNameMatch = getDSMatchForDriverClass(connProfileDriverClass);
                if(dsNameMatch!=null) {
                    typeName=dsNameMatch;
                }
            }
        }
        // Verify the "typeName" exists.
        if (!this.dataSourceTypeNames.contains(typeName)) {
            if("connector-jdbc".equals(typeName)) {  //$NON-NLS-1$
                throw new TeiidExecutionException(
                		ITeiidDataSource.ERROR_CODES.JDBC_DRIVER_SOURCE_NOT_FOUND,
                		Messages.getString(Messages.ExecutionAdmin.jdbcSourceForClassNameNotFound, connProfileDriverClass, getServer()));
            } else {
                throw new TeiidExecutionException(
                		ITeiidDataSource.ERROR_CODES.DATA_SOURCE_TYPE_DOES_NOT_EXIST_ON_TEIID,
                		Messages.getString(Messages.ExecutionAdmin.dataSourceTypeDoesNotExist, typeName, getServer()));
            }
        }

        this.admin.createDataSource(dsName, typeName, properties);

        refreshDataSourceNames();

        // Check that local name list contains new dsName
        if (dataSourceExists(dsName)) {
            String nullStr = null;
            ITeiidDataSource tds = new TeiidDataSource(nullStr, dsName, typeName, properties);

            this.dataSourceByNameMap.put(dsName, tds);
            this.eventManager.notifyListeners(ExecutionConfigurationEvent.createAddDataSourceEvent(tds));

            return tds;
        }

        // We shouldn't get here if data source was created
        throw new TeiidExecutionException(
        		ITeiidDataSource.ERROR_CODES.DATA_SOURCE_COULD_NOT_BE_CREATED,
        		Messages.getString(Messages.ExecutionAdmin.errorCreatingDataSource, dsName, typeName));
    }

    /**
     * Look for an installed driver that has the driverClass which matches the supplied driverClass name.
     * 
     * @param requestDriverClass the driver class to match
     * @return the name of the matching driver, null if not found
     */
    private String getDSMatchForDriverClass(String requestDriverClass) throws Exception {
        if (requestDriverClass == null)
            return null;

        if (!getServer().isParentConnected())
            return null;

        try {
            Collection<IDataSourceDriver> dataSourceDrivers = adminSpec.getDataSourceDrivers(admin);
            for (IDataSourceDriver driver : dataSourceDrivers) {
                String driverClassName = driver.getClassName();
                String driverName = driver.getName();

                if (requestDriverClass.equalsIgnoreCase(driverClassName))
                    return driverName;
            }

        } catch (Exception ex) {
            // Failed to get mapping
            KLog.getLogger().error(Messages.getString(Messages.ExecutionAdmin.failedToGetDriverMappings, requestDriverClass), ex);
        }

        return null;
    }
    
    /*
     * Deploy all jars in the supplied jarList
     * @param admin the Admin instance
     * @param jarList the colon-separated list of jar path locations
     */
    private void deployJars(Admin admin, String jarList) {
        // Path Entries are colon separated
        String[] jarPathStrs = jarList.split("[:]");  //$NON-NLS-1$

        // Attempt to deploy each jar
        for(String jarPathStr: jarPathStrs) {
            File theFile = new File(jarPathStr);
            if(theFile.exists()) {
                if(theFile.canRead()) {
                    String fileName = theFile.getName();
                    InputStream iStream = null;
                    try {
                        iStream = new FileInputStream(theFile);
                    } catch (FileNotFoundException ex) {
                        KLog.getLogger().error(Messages.getString(Messages.ExecutionAdmin.jarDeploymentJarNotFound, theFile.getPath()), ex);
                        continue;
                    }
                    try {
                        adminSpec.deploy(admin, fileName, iStream);
                    } catch (Exception ex) {
                        // Jar deployment failed
                        KLog.getLogger().error(Messages.getString(Messages.ExecutionAdmin.jarDeploymentFailed, theFile.getPath()), ex);
                    }
                } else {
                    // Could not read the file
                    KLog.getLogger().error(Messages.getString(Messages.ExecutionAdmin.jarDeploymentJarNotReadable, theFile.getPath()));
                }
            } else {
                // The file was not found
                KLog.getLogger().error(Messages.getString(Messages.ExecutionAdmin.jarDeploymentJarNotFound, theFile.getPath()));
            }

        }
    }
    
    @Override
    public void deployDriver(File jarOrRarFile) throws Exception {
        if(jarOrRarFile.exists()) {
            if(jarOrRarFile.canRead()) {
                String fileName = jarOrRarFile.getName();
                InputStream iStream = null;
                try {
                    iStream = new FileInputStream(jarOrRarFile);
                } catch (FileNotFoundException ex) {
                    KLog.getLogger().error(Messages.getString(Messages.ExecutionAdmin.jarDeploymentJarNotFound, jarOrRarFile.getPath()), ex);
                    throw ex;
                }
                try {
                    adminSpec.deploy(admin, fileName, iStream);
                    refreshDataSourceTypes();
                } catch (Exception ex) {
                    // Jar deployment failed
                    KLog.getLogger().error(Messages.getString(Messages.ExecutionAdmin.jarDeploymentFailed, jarOrRarFile.getPath()), ex);
                    throw ex;
                }
            } else {
                // Could not read the file
                KLog.getLogger().error(Messages.getString(Messages.ExecutionAdmin.jarDeploymentJarNotReadable, jarOrRarFile.getPath()));
            }
        } else {
            // The file was not found
            KLog.getLogger().error(Messages.getString(Messages.ExecutionAdmin.jarDeploymentJarNotFound, jarOrRarFile.getPath()));
        }
    }

    /**
     * @return the teiid instance who owns this admin object (never <code>null</code>)
     */
    public ITeiidInstance getServer() {
        return this.teiidInstance;
    }

    @Override
    public ITeiidTranslator getTranslator( String name ) {
        ArgCheck.isNotEmpty(name, "name"); //$NON-NLS-1$
        return this.translatorByNameMap.get(name);
    }

    @Override
    public Collection<ITeiidTranslator> getTranslators() {
        return Collections.unmodifiableCollection(translatorByNameMap.values());
    }

    @Override
    public Set<String> getDataSourceTemplateNames() throws Exception {
        return this.admin.getDataSourceTemplateNames();
    }
    
    @Override
    public Collection<TeiidPropertyDefinition> getTemplatePropertyDefns(String templateName) throws Exception {
        Collection<? extends PropertyDefinition> propDefs = this.admin.getTemplatePropertyDefinitions(templateName);

        Collection<TeiidPropertyDefinition> teiidPropDefns = new ArrayList<TeiidPropertyDefinition>();
        
        for (PropertyDefinition propDefn : propDefs) {
            TeiidPropertyDefinition teiidPropertyDefn = new TeiidPropertyDefinition();
            
            teiidPropertyDefn.setName(propDefn.getName());
            teiidPropertyDefn.setDisplayName(propDefn.getDisplayName());
            teiidPropertyDefn.setDescription(propDefn.getDescription());
            teiidPropertyDefn.setPropertyTypeClassName(propDefn.getPropertyTypeClassName());
            teiidPropertyDefn.setDefaultValue(propDefn.getDefaultValue());
            teiidPropertyDefn.setAllowedValues(propDefn.getAllowedValues());
            teiidPropertyDefn.setModifiable(propDefn.isModifiable());
            teiidPropertyDefn.setConstrainedToAllowedValues(propDefn.isConstrainedToAllowedValues());
            teiidPropertyDefn.setAdvanced(propDefn.isAdvanced());
            teiidPropertyDefn.setRequired(propDefn.isRequired());
            teiidPropertyDefn.setMasked(propDefn.isMasked());
            
            teiidPropDefns.add(teiidPropertyDefn);
        }
        
        return teiidPropDefns;
    }

    /*
     * (non-Javadoc)
     * @see org.teiid.designer.runtime.spi.IExecutionAdmin#getDataSourceProperties(java.lang.String)
     */
    @Override
    public Properties getDataSourceProperties(String name) throws Exception {
        if (isLessThanTeiidEight()) {
            // Teiid 7.7.x does not support
            return null;
        }

        return this.admin.getDataSource(name);
    }

    @Override
    public ITeiidVdb getVdb( String name ) {
        ArgCheck.isNotEmpty(name, "name"); //$NON-NLS-1$

        return teiidVdbs.get(name);
    }
    
    @Override
    public boolean hasVdb(String name) throws Exception {
        return getVdb(name) != null;
    }
    
    @Override
    public boolean isVdbActive(String vdbName) throws Exception {
        if (! hasVdb(vdbName))
            return false;
        
        return getVdb(vdbName).isActive();
    }
    
    @Override
    public boolean isVdbLoading(String vdbName) throws Exception {
        if (! hasVdb(vdbName))
            return false;
        
        return getVdb(vdbName).isLoading();
    }
    
    @Override
    public boolean hasVdbFailed(String vdbName) throws Exception {
        if (! hasVdb(vdbName))
            return false;
        
        return getVdb(vdbName).hasFailed();
    }
    
    @Override
    public boolean wasVdbRemoved(String vdbName) throws Exception {
        if (! hasVdb(vdbName))
            return false;
        
        return getVdb(vdbName).wasRemoved();
    }
    
    @Override
    public List<String> retrieveVdbValidityErrors(String vdbName) throws Exception {
        if (! hasVdb(vdbName))
            return Collections.emptyList();
        
        return getVdb(vdbName).getValidityErrors();
    }

    @Override
    public Collection<ITeiidVdb> getVdbs() {
        return Collections.unmodifiableCollection(teiidVdbs.values());
    }
    
    private void init() throws Exception {
        this.translatorByNameMap = new HashMap<String, ITeiidTranslator>();
        this.dataSourceNames = new ArrayList<String>();
        this.dataSourceByNameMap = new HashMap<String, ITeiidDataSource>();
        this.dataSourceTypeNames = new HashSet<String>();
        refreshVDBs();
    }

    private void internalSetPropertyValue( ITeiidTranslator translator,
                                           String propName,
                                           String value,
                                           TranslatorPropertyType type,
                                           boolean notify ) throws Exception {
        if (translator.isValidPropertyValue(propName, value, type) == null) {
            String oldValue = translator.getPropertyValue(propName, type);

            // don't set if value has not changed
            if (oldValue == null) {
                if (value == null) return;
            } else if (oldValue.equals(value)) return;

            if (notify) {
                // TODO: Will we ever update Translator properties in TEIID teiid instance?
                // this.eventManager.notifyListeners(ExecutionConfigurationEvent.createUpdateConnectorEvent(translator));
            }
        } else {
            throw new Exception(Messages.getString(Messages.ExecutionAdmin.invalidPropertyValue, value, propName));
        }
    }

    /**
     * @throws Exception if refreshing admin connection fails
     */
    @Override
    public void connect() throws Exception {
        if (!this.loaded) {
            refresh();
            this.loaded = true;
        }
    }

    /**
     * Refreshes the cached lists and maps of current Teiid objects
     * @throws Exception if refreshing admin connection fails
     */
    public void refresh() throws Exception {
        // populate translator map
        refreshTranslators(this.admin.getTranslators());

        // populate data source type names set
        refreshDataSourceTypes();

        // populate data source names list
        refreshDataSourceNames();

        this.dataSourceByNameMap.clear();
        Collection<ITeiidDataSource> tdsList = connectionMatcher.findTeiidDataSources(this.dataSourceNames);
        for (ITeiidDataSource ds : tdsList) {
            if (!isLessThanTeiidEight()) {
                /* Not done in Teiid 7.7 */
                // Get Properties for the source
                Properties dsProps = this.admin.getDataSource(ds.getName());
                // Transfer properties to the ITeiidDataSource
                ds.getProperties().clear();
                ds.getProperties().putAll(dsProps);
            }

        	// put ds into map
            this.dataSourceByNameMap.put(ds.getName(), ds);
        }

        // populate VDBs and source bindings
        refreshVDBs();

        // notify listeners
        this.eventManager.notifyListeners(ExecutionConfigurationEvent.createTeiidRefreshEvent(this.teiidInstance));
    }

    protected void refreshDataSourceNames() throws Exception {
        // populate data source names list
        this.dataSourceNames = new ArrayList(this.admin.getDataSourceNames());
    }

    /**
     * Refreshes the local collection of Translators on the referenced Teiid teiid instance.
     * 
     * @param translators
     * @throws Exception
     */
    protected void refreshTranslators( Collection<? extends Translator> translators ) throws Exception {
        for (Translator translator : translators) {
            if (translator.getName() != null) {
                if( teiidInstance.getVersion().isLessThan(Version.TEIID_8_6.get())) {
                	Collection<? extends PropertyDefinition> propDefs = this.admin.getTemplatePropertyDefinitions(translator.getName());
                	this.translatorByNameMap.put(translator.getName(), new TeiidTranslator(translator, propDefs, teiidInstance));
                } else if( teiidInstance.getVersion().isLessThan(Version.TEIID_8_7.get())) {
                	Collection<? extends PropertyDefinition> propDefs = this.admin.getTranslatorPropertyDefinitions(translator.getName());
                	this.translatorByNameMap.put(translator.getName(), new TeiidTranslator(translator, propDefs, teiidInstance));
                } else { // TEIID teiid instance VERSION 8.7 AND HIGHER
                	Collection<? extends PropertyDefinition> propDefs  = 
                			this.admin.getTranslatorPropertyDefinitions(translator.getName(), Admin.TranlatorPropertyType.OVERRIDE);
                	Collection<? extends PropertyDefinition> importPropDefs  = 
                			this.admin.getTranslatorPropertyDefinitions(translator.getName(), Admin.TranlatorPropertyType.IMPORT);
                	Collection<? extends PropertyDefinition> extPropDefs  = 
                			this.admin.getTranslatorPropertyDefinitions(translator.getName(), Admin.TranlatorPropertyType.EXTENSION_METADATA);
                	this.translatorByNameMap.put(translator.getName(), new TeiidTranslator(translator, propDefs, importPropDefs, extPropDefs, teiidInstance));
                }
            }
        }
    }

    protected void refreshVDBs() throws Exception {
        Collection<? extends VDB> vdbs = Collections.unmodifiableCollection(this.admin.getVDBs());
        
        teiidVdbs = new HashMap<String, ITeiidVdb>();

        for (VDB vdb : vdbs) {
            teiidVdbs.put(vdb.getName(), new TeiidVdb(vdb, teiidInstance));
        }
    }
    
    protected void refreshDataSourceTypes() throws Exception {
        // populate data source type names set
        this.dataSourceTypeNames = new HashSet(this.admin.getDataSourceTemplateNames());
    }

    /**
     * @param translator the translator whose properties are being changed (never <code>null</code>)
     * @param changedProperties a collection of properties that have changed (never <code>null</code> or empty)
     * @param type the translator property type
     * @throws Exception if there is a problem changing the properties
     *
     */
    public void setProperties( ITeiidTranslator translator,
                               Properties changedProperties,
                               TranslatorPropertyType type) throws Exception {
        ArgCheck.isNotNull(translator, "translator"); //$NON-NLS-1$
        ArgCheck.isNotNull(changedProperties, "changedProperties"); //$NON-NLS-1$
        ArgCheck.isNotEmpty(changedProperties.entrySet(), "changedProperties"); //$NON-NLS-1$

        if (changedProperties.size() == 1) {
            String name = changedProperties.stringPropertyNames().iterator().next();
            setPropertyValue(translator, name, changedProperties.getProperty(name), type);
        } else {

            for (String name : changedProperties.stringPropertyNames()) {
                internalSetPropertyValue(translator, name, changedProperties.getProperty(name), type, false);
            }
            // this.eventManager.notifyListeners(ExecutionConfigurationEvent.createUpdateConnectorEvent(translator));
        }
    }

    /**
     * @param translator the translator whose property is being changed (never <code>null</code>)
     * @param propName the name of the property being changed (never <code>null</code> or empty)
     * @param value the new value
     * @param type the translator property type
     * @throws Exception if there is a problem setting the property
     *
     */
    public void setPropertyValue( ITeiidTranslator translator,
                                  String propName,
                                  String value,
                                  TranslatorPropertyType type) throws Exception {
        ArgCheck.isNotNull(translator, "translator"); //$NON-NLS-1$
        ArgCheck.isNotEmpty(propName, "propName"); //$NON-NLS-1$
        ArgCheck.isNotEmpty(value, "value"); //$NON-NLS-1$
        internalSetPropertyValue(translator, propName, value, type, true);
    }

    @Override
    public void undeployVdb( String vdbName) throws Exception {
        ITeiidVdb vdb = getVdb(vdbName);
        if(vdb!=null) {
        	adminSpec.undeploy(admin, appendVdbExtension(vdbName), vdb.getVersion());
        }
        vdb = getVdb(vdbName);

        refreshVDBs();

        if (vdb == null) {

        } else {
            this.eventManager.notifyListeners(ExecutionConfigurationEvent.createUnDeployVDBEvent(vdb.getName()));
        }
    }
    
    @Override
    public void undeployDynamicVdb( String vdbName) throws Exception {
        ITeiidVdb vdb = getVdb(vdbName);
        if(vdb!=null) {
        	adminSpec.undeploy(admin, appendDynamicVdbSuffix(vdbName), vdb.getVersion());
        }
        vdb = getVdb(vdbName);

        refreshVDBs();

        if (vdb == null) {

        } else {
            this.eventManager.notifyListeners(ExecutionConfigurationEvent.createUnDeployVDBEvent(vdb.getName()));
        }
    }

    /**
     * 
     * @param vdbName the vdb name
     * @param vdbVersion the vdb version
     * @throws Exception if undeploying vdb fails
     */
    public void undeployVdb( String vdbName, int vdbVersion ) throws Exception {
        adminSpec.undeploy(admin, appendVdbExtension(vdbName), vdbVersion);
        ITeiidVdb vdb = getVdb(vdbName);

        refreshVDBs();

        if (vdb == null) {

        } else {
            this.eventManager.notifyListeners(ExecutionConfigurationEvent.createUnDeployVDBEvent(vdb.getName()));
        }
    }
    
    /**
     * Append the vdb file extension to the vdb name 
     * if not already appended.
     * 
     * @param vdbName
     * @return
     */
    private String appendVdbExtension(String vdbName) {
        if (vdbName.endsWith(ITeiidVdb.VDB_EXTENSION))
            return vdbName;
        
        return vdbName + ITeiidVdb.VDB_DOT_EXTENSION;
    }
    
    /**
     * Append the suffix for dynamic VDB to the vdb name if not already appended.
     * 
     * @param vdbName
     * @return
     */
    private String appendDynamicVdbSuffix(String vdbName) {
        if (vdbName.endsWith(ITeiidVdb.DYNAMIC_VDB_SUFFIX))
            return vdbName;
        
        return vdbName + ITeiidVdb.DYNAMIC_VDB_SUFFIX;
    }
    
    @Override
    public IOutcome ping(PingType pingType) {
        String msg = Messages.getString(Messages.ExecutionAdmin.cannotConnectToServer, teiidInstance.getTeiidAdminInfo().getUsername());
        try {
            if (this.admin == null)
                throw new Exception(msg);
            
            switch(pingType) {
                case JDBC:
                    return pingJdbc();
                case ADMIN:
                default:
                    return pingAdmin();
            }
        }
        catch (Exception ex) {
            return OutcomeFactory.getInstance().createError(msg, ex);
        }
    }
    
    private IOutcome pingAdmin() throws Exception {
        admin.getSessions();
        return OutcomeFactory.getInstance().createOK();
    }
    
    private IOutcome pingJdbc() {
        String host = teiidInstance.getHost();
        ITeiidJdbcInfo teiidJdbcInfo = teiidInstance.getTeiidJdbcInfo();
        
        String protocol = ITeiidConnectionInfo.MM;
        if (teiidJdbcInfo.isSecure())
            protocol = ITeiidConnectionInfo.MMS;

        Connection teiidJdbcConnection = null;
        String url = "jdbc:teiid:ping@" + protocol + host + ':' + teiidJdbcInfo.getPort(); //$NON-NLS-1$
        
        try {

            admin.deploy(PING_VDB, new ByteArrayInputStream(adminSpec.getTestVDB().getBytes()));
            
            try{
                String urlAndCredentials = url + ";user=" + teiidJdbcInfo.getUsername() + ";password=" + teiidJdbcInfo.getPassword() + ';';  //$NON-NLS-1$ //$NON-NLS-2$              
                TeiidDriver teiidDriver = TeiidDriver.getInstance();
                teiidDriver.setTeiidVersion(teiidInstance.getVersion());
                teiidJdbcConnection = teiidDriver.connect(urlAndCredentials, null);
               //pass
            } catch(SQLException ex){
                String msg = Messages.getString(Messages.ExecutionAdmin.instanceDeployUndeployProblemPingingTeiidJdbc, url);
                return OutcomeFactory.getInstance().createError(msg, ex);
            } finally {
                adminSpec.undeploy(admin, PING_VDB, 1);
                
                if( teiidJdbcConnection != null ) {
                    teiidJdbcConnection.close();
                }
            }
        } catch (Exception ex) {
            String msg = Messages.getString(Messages.ExecutionAdmin.instanceDeployUndeployProblemPingingTeiidJdbc, url);
            return OutcomeFactory.getInstance().createError(msg, ex);
        }
        
        return OutcomeFactory.getInstance().createOK();
    }
    
    @Override
    public String getAdminDriverPath() {
        return Admin.class.getProtectionDomain().getCodeSource().getLocation().getFile();
    }
    
    @Override
    public Driver getTeiidDriver(String driverClass) throws Exception {
        Class<?> klazz = getClass().getClassLoader().loadClass(driverClass);
        Object driver = klazz.newInstance();
        if (driver instanceof Driver)
            return (Driver) driver;
        
        throw new Exception(Messages.getString(Messages.ExecutionAdmin.cannotLoadDriverClass, driverClass));
    }

    @Override
    @Deprecated
    @Removed(Version.TEIID_8_0)
    public void mergeVdbs( String sourceVdbName, int sourceVdbVersion, 
                                            String targetVdbName, int targetVdbVersion ) throws Exception {
        if (!AnnotationUtils.isApplicable(getClass().getMethod("mergeVdbs"), getServer().getVersion()))  //$NON-NLS-1$
            throw new UnsupportedOperationException(Messages.getString(Messages.ExecutionAdmin.mergeVdbUnsupported));

        admin.mergeVDBs(sourceVdbName, sourceVdbVersion, targetVdbName, targetVdbVersion);        
    }

    private class RefreshThread extends Thread {

        private VDB vdb;

        private String vdbName;

        private int vdbVersion;

        /**
         * @param vdb
         */
        public RefreshThread(VDB vdb) {
            this.vdb = vdb;
            this.vdbName = vdb.getName();
            this.vdbVersion = vdb.getVersion();

            this.setDaemon(true);
            this.setName("Refreshing vdb + " + vdb.getName()); //$NON-NLS-1$
        }

      /*
      * Wait for the VDB to finish loading.  Will check status every 5 secs and return when the VDB is loaded.
      * If not loaded within 30sec, it will timeout
      * @param vdbName the name of the VDB
      */
        private void waitForVDBLoad() throws Exception {
            long waitUntil = System.currentTimeMillis() + VDB_LOADING_TIMEOUT_SEC * 1000;
            boolean first = true;
            do {
                // Pauses 5 sec
                if (!first) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        break;
                    }
                } else {
                    first = false;
                }

                // Refreshes from adminApi
                refreshVDBs();

                // Get the teiid vdb
                ITeiidVdb vdb = getVdb(vdbName);
                // Stop waiting if any conditions have been met
                if (vdb == null)
                    return;

                boolean hasValidityErrors = !vdb.getValidityErrors().isEmpty();
                if (!vdb.hasModels() || vdb.hasFailed() || !vdb.isLoading() || vdb.isActive() || vdb.wasRemoved() || hasValidityErrors) {
                    return;
                }
            } while (System.currentTimeMillis() < waitUntil);

            refreshVDBs();
            return;
        }

        private boolean isVdbLoading() throws Exception {
            // Get the refreshed vdb straight off the server
            vdb = admin.getVDB(vdbName, vdbVersion);
            if (vdb == null)
                return false;

            return adminSpec.getLoadingVDBStatus().equals(vdb.getStatus()) && vdb.getValidityErrors().isEmpty();
        }

        @Override
        public void run() {
            try {
                // If the VDB is still loading, refresh again and potentially start refresh job
                if(isVdbLoading()) {
                    // Give a 0.5 sec pause for the VDB to finish loading metadata.
                    Thread.sleep(500);
                } 
            }   catch (Exception e) {
                KLog.getLogger().error(Messages.getString(Messages.ExecutionAdmin.refreshVdbException, vdbName), e);
            }

            try {
                // Refresh again to update vdb states
                refreshVDBs();

                // Determine if still loading, if so wait for loading to be completed
                if(isVdbLoading()) {
                    waitForVDBLoad();
                }
            } catch (Exception ex) {
                KLog.getLogger().error(Messages.getString(Messages.ExecutionAdmin.refreshVdbException, vdbName), ex);
            }

            eventManager.notifyListeners(ExecutionConfigurationEvent.createDeployVDBEvent(vdb.getName()));
        }

    }
    
}
