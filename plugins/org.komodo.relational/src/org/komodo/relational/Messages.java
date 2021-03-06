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
package org.komodo.relational;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.komodo.spi.constants.StringConstants;

/**
 *
 */
public class Messages implements StringConstants {

	private static final String BUNDLE_NAME = Messages.class.getPackage().getName()
			+ DOT
			+ Messages.class.getSimpleName().toLowerCase();

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    @SuppressWarnings( "javadoc" )
    public enum RELATIONAL {
    	RelationalStringNameValidator_or_other_valid_table_characters,
    	RelationalStringNameValidator_nameIsInvalidTheCharacterAt,
    	datatypeProcessor_error_while_computing_datatype,
    	datatypeProcessor_error_finding_datatatype,
    	relationalModelFactory_unknown_object_type_0_cannot_be_processed,
    	relationalModelFactory_error_finding_table_named,
    	relationalModelFactory_error_adding_desciption_to_0,
    	relationalModelFactory_creatingModelChildren,
    	relationalModelFactory_creatingForeigneKeys,
    	relationalModelFactory_creatingIndexes,
    	relationalModelFactory_replacingModelObject,
    	relationalModelFactory_creatingModelChild,
    	relationalModelFactory_error_setting_extension_props_on_0,
    	relationalRefFactory_columnNotFound_forFKCreate,
    	relationalRefFactory_columnNotFound_forPKCreate,
    	relationalRefFactory_columnNotFound_forAPCreate,
    	relationalRefFactory_columnNotFound_forIndexCreate,
    	relationalRefFactory_errorSettingDescription,
    	relationalRefFactory_errorGettingEmfExtProps,
    	validationOkCreateObject,
    	validate_error_nameCannotBeNullOrEmpty,
    	validate_error_pkNoColumnsDefined,
    	validate_error_fkNoColumnsDefined,
    	validate_error_ucNoColumnsDefined,
    	validate_error_materializedTableHasNoTableDefined,
    	validate_warning_noColumnsDefined,
    	validate_warning_noColumnsDefinedForResultSet,
    	validate_error_fKUniqueKeyNameIsUndefined,
    	validate_error_fKReferencedUniqueKeyTableIsUndefined,
    	validate_error_duplicateColumnNamesInTable,
    	validate_error_duplicateParameterNamesInProcedure,
    	validate_warning_noParametersDefined,
    	validate_error_invalidParameterDirectionInFunction,
    	validate_error_tooManyResultParametersInFunction,
    	validate_noResultSetAllowedInFunction,
    	validate_error_duplicateColumnNamesReferencedInIndex,
    	validate_warning_noColumnReferencesDefined,
    	validate_categoryUndefinedForUDF,
    	validate_javaClassUndefinedForUDF,
    	validate_javaMethodUndefinedForUDF,
    	invalidTargetTypeForGetUpdatedTeiidMethod;

    	@Override
    	public String toString() {
    		return getEnumName(this) + DOT + name();
    	}
    }

    private static String getEnumName(Enum<?> enumValue) {
        String className = enumValue.getClass().getName();
        String[] components = className.split("\\$"); //$NON-NLS-1$
        return components[components.length - 1];
    }

    private Messages() {
    }

    /**
     * Get message string
     *
     * @param key
     *
     * @return i18n string
     */
    private static String getString(Enum<?> key) {
        try {
            return RESOURCE_BUNDLE.getString(key.toString());
        } catch (final Exception err) {
            String msg;

            if (err instanceof NullPointerException) {
                msg = "<No message available>"; //$NON-NLS-1$
            } else if (err instanceof MissingResourceException) {
                msg = OPEN_ANGLE_BRACKET + "Missing message for key \"" + key + "\" in: " + BUNDLE_NAME + CLOSE_ANGLE_BRACKET; //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                msg = err.getLocalizedMessage();
            }

            return msg;
        }
    }

    /**
     * Get message string with parameters
     *
     * @param key the enum key
     * @param parameters params
     *
     * @return i18n string
     */
    public static String getString(Enum<?> key, Object... parameters) {
        String text = getString(key);

        // Check the trivial cases ...
        if (text == null) {
            return OPEN_ANGLE_BRACKET + key.toString() + CLOSE_ANGLE_BRACKET;
        }
        if (parameters == null || parameters.length == 0) {
            return text;
        }

        return MessageFormat.format(text, parameters);
    }
}
