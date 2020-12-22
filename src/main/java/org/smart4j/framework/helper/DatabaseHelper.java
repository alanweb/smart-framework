package org.smart4j.framework.helper;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smart4j.framework.util.CollectionUtil;
import org.smart4j.framework.util.PropsUtil;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * 数据库操作助手类
 */
public final class DatabaseHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseHelper.class);
    private static final QueryRunner QUERY_RUNNER;
    private static final ThreadLocal<Connection> CONNECTION_HOLDER;
    private static final BasicDataSource DATA_SOURCE;

    static {
        QUERY_RUNNER = new QueryRunner();
        CONNECTION_HOLDER = new ThreadLocal<>();

        Properties props = PropsUtil.loadProps("config.properties");
        String driver = props.getProperty("jdbc.driver");
        String url = props.getProperty("jdbc.url");
        String username = props.getProperty("jdbc.username");
        String password = props.getProperty("jdbc.password");
        DATA_SOURCE = new BasicDataSource();
        DATA_SOURCE.setDriverClassName(driver);
        DATA_SOURCE.setUrl(url);
        DATA_SOURCE.setUsername(username);
        DATA_SOURCE.setPassword(password);
//        try {
//            Class.forName(driver);
//        } catch (ClassNotFoundException e) {
//            LOGGER.error("can not load jdbc driver", e);
//        }
    }

    /**
     * 获取数据库连接
     *
     * @return
     */
    public static Connection getConnection() {
        Connection conn = CONNECTION_HOLDER.get();
        if (conn == null)
            try {
//                conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                conn = DATA_SOURCE.getConnection();
            } catch (SQLException e) {
                LOGGER.error("get connection failure", e);
                throw new RuntimeException(e);
            } finally {
                CONNECTION_HOLDER.set(conn);
            }
        return conn;
    }

    /**
     * 查询实体列表
     *
     * @return
     */
    public static <T> List<T> queryEntityList(Class<T> clazz, String sql, Object... params) {
        List<T> list = null;
        try {
            list = QUERY_RUNNER.query(getConnection(), sql, new BeanListHandler<T>(clazz), params);
        } catch (SQLException e) {
            LOGGER.error("query entity list failure", e);
            throw new RuntimeException(e);
        }
        return list;
    }

    /**
     * 查询实体
     *
     * @return
     */
    public static <T> T queryEntity(Class<T> clazz, String sql, Object... params) {
        T entity = null;
        try {
            entity = QUERY_RUNNER.query(getConnection(), sql, new BeanHandler<>(clazz), params);
        } catch (SQLException e) {
            LOGGER.error("query entity failure", e);
            throw new RuntimeException(e);
        }
        return entity;
    }

    /**
     * 执行查询语句
     *
     * @param sql
     * @param params
     * @return
     */
    public static List<Map<String, Object>> executeQuery(String sql, Object... params) {
        List<Map<String, Object>> result = null;
        try {
            result = QUERY_RUNNER.query(getConnection(), sql, new MapListHandler(), params);
        } catch (SQLException e) {
            LOGGER.error("execute query failure", e);
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 执行单条件单列单返回值查询语句
     *
     * @param sql
     * @param param
     * @return
     */
    public static String query(String sql, Object param) {
        String result = null;
        try {
            result = QUERY_RUNNER.query(getConnection(), sql, new BeanHandler<>(String.class), param);
        } catch (SQLException e) {
            LOGGER.error("query failure", e);
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 执行单条件单列多返回值查询语句
     *
     * @param sql
     * @param param
     * @return
     */
    public static Set<String> querySet(String sql, Object param) {
        Set<String> result = null;
        try {
            List<String> list = QUERY_RUNNER.query(getConnection(), sql, new BeanListHandler<>(String.class), param);
            result = new HashSet<>(list);
        } catch (SQLException e) {
            LOGGER.error("query failure", e);
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 执行修改语句
     *
     * @param sql
     * @param params
     * @return
     */
    public static int executeUpdate(String sql, Object... params) {
        int rows = 0;
        try {
            rows = QUERY_RUNNER.update(getConnection(), sql, params);
        } catch (SQLException e) {
            LOGGER.error("execute update failure", e);
            throw new RuntimeException(e);
        }
        return rows;
    }

    public static void executeFile(String fileName) {
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String sql = null;
            while ((sql = reader.readLine()) != null) {
                QUERY_RUNNER.update(getConnection(), sql);
            }
        } catch (Exception e) {
            LOGGER.error("execute file failure", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 插入实体
     *
     * @param clazz
     * @param fieldMap
     * @return
     */
    public static <T> boolean insertEntity(Class<T> clazz, Map<String, Object> fieldMap) {
        if (CollectionUtil.isEmpty(fieldMap)) {
            LOGGER.error("can not insert entity: fieldMap is empty");
            return false;
        }
        String sql = "INSERT INTO " + getTableName(clazz);
        StringBuilder columns = new StringBuilder("(");
        StringBuilder values = new StringBuilder("(");
        for (String fieldName : fieldMap.keySet()) {
            columns.append(fieldName).append(", ");
            values.append("?, ");
        }
        columns.replace(columns.lastIndexOf(", "), columns.length(), ")");
        values.replace(values.lastIndexOf(", "), values.length(), ")");
        sql = sql + columns + " VALUES " + values;
        Object[] params = fieldMap.values().toArray();
        return executeUpdate(sql, params) == 1;
    }

    /**
     * 更新实体
     *
     * @param id
     * @param clazz
     * @param fieldMap
     * @return
     */
    public static <T> boolean updateEntity(long id, Class<T> clazz, Map<String, Object> fieldMap) {
        if (CollectionUtil.isEmpty(fieldMap)) {
            LOGGER.error("can not update entity: fieldMap is empty");
            return false;
        }
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(getTableName(clazz)).append(" SET ");
        StringBuilder columns = new StringBuilder();
        for (String fieldName : fieldMap.keySet()) {
            columns.append(fieldName).append("=?, ");
        }
        sql.append(columns.substring(0, columns.length() - 2)).append(" WHERE id = ?");
        List<Object> paramList = new ArrayList<>();
        paramList.addAll(fieldMap.values());
        paramList.add(id);
        Object[] params = paramList.toArray();
        return executeUpdate(sql.toString(), params) == 1;
    }

    /**
     * 更新实体
     *
     * @param id
     * @param clazz
     * @return
     */
    public static <T> boolean deleteEntity(long id, Class<T> clazz) {
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(getTableName(clazz)).append(" WHERE id = ?");
        return executeUpdate(sql.toString(), id) == 1;
    }

    private static <T> String getTableName(Class<T> clazz) {
        return underscoreName(clazz.getSimpleName());
    }

    public static String underscoreName(String name) {
        StringBuilder result = new StringBuilder("T_");
        if (name != null && name.length() > 0) {
            // 将第一个字符处理成大写
            result.append(name.substring(0, 1).toUpperCase());
            // 循环处理其余字符
            for (int i = 1; i < name.length(); i++) {
                String s = name.substring(i, i + 1);
                // 在大写字母前添加下划线
                if (s.equals(s.toUpperCase()) && !Character.isDigit(s.charAt(0))) {
                    result.append("_");
                }
                // 其他字符直接转成大写
                result.append(s.toUpperCase());
            }
        }
        return result.toString();
    }

    /**
     * 开启事务
     */
    public static void beginTransaction() {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                conn.setAutoCommit(false);
            } catch (SQLException e) {
                LOGGER.error("begin transaction failure", e);
                throw new RuntimeException(e);
            } finally {
                CONNECTION_HOLDER.set(conn);
            }
        }
    }

    /**
     * 提交事务
     */
    public static void commitTransaction() {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                conn.commit();
                conn.close();
            } catch (SQLException e) {
                LOGGER.error("commit transaction failure", e);
                throw new RuntimeException(e);
            } finally {
                CONNECTION_HOLDER.remove();
            }
        }
    }

    /**
     * 回滚事务
     */
    public static void rollbackTransaction() {
        Connection conn = getConnection();
        if (conn != null) {
            try {
                conn.rollback();
                conn.close();
            } catch (SQLException e) {
                LOGGER.error("rollback transaction failure", e);
                throw new RuntimeException(e);
            } finally {
                CONNECTION_HOLDER.remove();
            }
        }
    }

    public static DataSource getDataSource() {
        return DATA_SOURCE;
    }
}
