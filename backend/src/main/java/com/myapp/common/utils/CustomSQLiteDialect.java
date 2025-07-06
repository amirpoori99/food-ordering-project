package com.myapp.common.utils;

import org.hibernate.community.dialect.SQLiteDialect;

/**
 * Custom SQLite dialect that disables constraint creation for SQLite compatibility
 */
public class CustomSQLiteDialect extends SQLiteDialect {
    
    @Override
    public String getAddForeignKeyConstraintString(String cn, String[] fk, String rt, String[] pk, boolean rpk) {
        // Disable FK constraints for SQLite
        return "";
    }

    @Override
    public String getAddPrimaryKeyConstraintString(String constraintName) {
        // Disable PK constraints for SQLite
        return "";
    }

    @Override
    public boolean dropConstraints() {
        // No need to drop constraints in SQLite
        return false;
    }
} 