package org.goobi.process;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Institution;
import org.goobi.beans.Process;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.IManager;
import de.sub.goobi.persistence.managers.MySQLHelper;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ExtendedProcessManager implements IManager {

    private static final long serialVersionUID = 8643226374054415902L;
    
    private String defaultValue;
    private boolean showFulltext;
    private boolean showThumbnail;

    public ExtendedProcessManager(String defaultValue, boolean showFulltext, boolean showThumbnail) {
        this.defaultValue = defaultValue;
        this.showFulltext = showFulltext;
        this.showThumbnail = showThumbnail;
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count, Institution institution) {
        try {
            return getProcesses(order, filter, start, count);
        } catch (SQLException e) {
            log.error(e);
            return Collections.emptyList();
        }
    }

    @Override
    public int getHitSize(String order, String filter, Institution institution) throws DAOException {
        try {
            return getProcessCount(filter);
        } catch (SQLException e) {
            log.error(e);
            return 0;
        }
    }

    public int getProcessCount(String filter) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT count(1) FROM prozesse WHERE ProzesseID in ( ");
        sql.append(filter);

        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public List<ExtendendProcess> getProcesses(String order, String filter, Integer start, Integer count) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT prozesse.* FROM prozesse WHERE ProzesseID in ( ");
        sql.append(filter);

        if (order != null && !order.isEmpty()) {
            sql.append(" ORDER BY " + order);
        }
        if (start != null && count != null) {
            sql.append(" LIMIT " + start + ", " + count);
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();
            List<Process> ret = new QueryRunner().query(connection, sql.toString(), ProcessManager.resultSetToProcessListHandler);
            List<ExtendendProcess> answer = new ArrayList<>(ret.size());
            for (Process p : ret) {
                answer.add(new ExtendendProcess(p, defaultValue, showFulltext, showThumbnail));
            }
            return answer;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    @Override
    public List<Integer> getIdList(String order, String filter, Institution institution) {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT prozesse.ProzesseID FROM prozesse WHERE ProzesseID in ( ");
        sql.append(filter);

        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerListHandler);
        } catch (SQLException e) {
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                    log.error(e);
                }
            }
        }
        return Collections.emptyList();
    }

}
