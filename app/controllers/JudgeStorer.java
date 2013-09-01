package controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import net.jiangwei.research.geo.tagging.database.GeoNamesConverter;

public class JudgeStorer {

	class JudgeTask {
		String url, name, geotag, correct, username;

		public JudgeTask(String url, String name, String geotag, String correct, String username) {
			super();
			this.url = url;
			this.name = name;
			this.geotag = geotag;
			this.correct = correct;
			this.username = username;
		}
	}

	private Queue<JudgeTask> tasks = new LinkedList<JudgeTask>();

	private boolean stop = false;

	private Thread storer;

	private Object lock = new Object();

	private JudgeStorer() {
		initProcess();
	}

	private static JudgeStorer instance = new JudgeStorer();

	public static JudgeStorer getInstance() {
		return instance;
	}

	private static Integer getResultID(Connection connection, String url,
			String name, String geotag) throws SQLException {
		String sql = "SELECT id FROM results WHERE url = ? AND name = ? and location = ?";
		ResultSet rs = null;
		PreparedStatement ps = null;
		Integer resultId = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1, url);
			ps.setString(2, name);
			ps.setString(3, geotag);
			rs = ps.executeQuery();
			if (rs.next()) {
				resultId = rs.getInt(1);
			}
		} finally {
			GeoNamesConverter.closeSqlWrapper(ps);
			GeoNamesConverter.closeSqlWrapper(rs);
		}
		if (resultId == null) {
			sql = "INSERT INTO results (url, name, location) VALUES (?, ?, ?)";
			try {
				ps = connection.prepareStatement(sql);
				ps.setString(1, url);
				ps.setString(2, name);
				ps.setString(3, geotag);
				ps.executeUpdate();
			} finally {
				GeoNamesConverter.closeSqlWrapper(ps);
			}

			try {
				ps = connection.prepareStatement("SELECT last_insert_rowid()");
				rs = ps.executeQuery();
				if (rs.next()) {
					resultId = rs.getInt(1);
				}
			} finally {
				GeoNamesConverter.closeSqlWrapper(ps);
				GeoNamesConverter.closeSqlWrapper(rs);
			}

		}
		return resultId;
	}

	private Integer getUserID(Connection connection, String username) throws SQLException {
		if (username == null || username.trim().length() == 0) return -1;
		String sql = "SELECT id FROM users WHERE username = ?";
		ResultSet rs = null;
		PreparedStatement ps = null;
		Integer resultId = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setString(1, username);
			rs = ps.executeQuery();
			if (rs.next()) {
				resultId = rs.getInt(1);
			}
		} finally {
			GeoNamesConverter.closeSqlWrapper(ps);
			GeoNamesConverter.closeSqlWrapper(rs);
		}
		if (resultId == null) {
			sql = "INSERT INTO users (username) VALUES (?)";
			try {
				ps = connection.prepareStatement(sql);
				ps.setString(1, username);
				ps.executeUpdate();
			} finally {
				GeoNamesConverter.closeSqlWrapper(ps);
			}

			try {
				ps = connection.prepareStatement("SELECT last_insert_rowid()");
				rs = ps.executeQuery();
				if (rs.next()) {
					resultId = rs.getInt(1);
				}
			} finally {
				GeoNamesConverter.closeSqlWrapper(ps);
				GeoNamesConverter.closeSqlWrapper(rs);
			}

		}
		return resultId;
	}
	
	private static void storeJudge(Connection connection, Integer resultId,
			Integer userId, Integer correct) throws SQLException {
		String sql = "INSERT INTO judges (result_id, user_id, judge, judge_date) VALUES (?, ?, ?, ?)";
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(sql);
			ps.setInt(1, resultId);
			ps.setInt(2, userId);
			ps.setInt(3, correct);
			ps.setDate(4, new java.sql.Date(new Date().getTime()));
			ps.executeUpdate();
			connection.commit();
		} finally {
			GeoNamesConverter.closeSqlWrapper(ps);
		}
	}

	private void store(JudgeTask task) {
		Connection connection = null;
		Integer resultId = null;
		Integer userId = null;
		try {
			connection = DBUtils.getConnection();
			resultId = getResultID(connection, task.url, task.name, task.geotag);
			userId = getUserID(connection, task.username);
			storeJudge(connection, resultId, userId, Integer.parseInt(task.correct));
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
			// return badRequest(Helper.getErrorMessage(nfe));
		} catch (SQLException e) {
			e.printStackTrace();
			this.stop = true;
			System.exit(-1);
			// return internalServerError(Helper.getErrorMessage(e));
		} finally {
			GeoNamesConverter.closeSqlWrapper(connection);
		}
	}


	private void initProcess() {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while (!stop) {
					if (tasks.size() == 0) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
					} else {
						JudgeTask task = null;
						synchronized (lock) {
							task = tasks.poll();
						}
						store(task);
					}
				}
			}
		};
		storer = new Thread(runnable);
		storer.start();
	}

	public void addTask(String url, String name, String geotag, String correct, String username) {
		synchronized (lock) {
			tasks.add(new JudgeTask(url, name, geotag, correct, username));
		}
	}
	
	public void stopStorer() {
		this.stop = true;
	}
}
