package com.tcl.mig.staticssdk.database;

//CHECKSTYLE:OFF
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.tcl.mig.staticssdk.DBAsyncTask;
import com.tcl.mig.staticssdk.DBAsyncTask.AsyncCallBack;
import com.tcl.mig.staticssdk.StaticDataContentProvider;
import com.tcl.mig.staticssdk.beans.CtrlBean;
import com.tcl.mig.staticssdk.beans.PostBean;
import com.tcl.mig.staticssdk.utiltool.UtilTool;

/**
 * 
 * <br>
 * 类描述:DB封装工具 <br>
 * 功能详细描述:
 * 
 * @author rongjinsong
 * @date [2013-3-4]
 */
public class DataBaseProvider {

	private Context mContext;
	private DataBaseHelper mHelp;
	private boolean mCanNotFindUrl = false;
	private ExecutorService mSingleExecutor;

	public DataBaseProvider(Context context) {
		mContext = context;
		mSingleExecutor = Executors.newSingleThreadExecutor();
	}

//	private synchronized DataBaseHelper getDataHelper() {
//		if (mHelp == null) {
//			mHelp = new DataBaseHelper(mContext);
//		}
//		return mHelp;
//	}

	public void insertPostDataAsync(final PostBean bean, AsyncCallBack callBack) {
		DBAsyncTask task = new DBAsyncTask();
		task.addCallBack(callBack);
		task.addTask(new Runnable() {
			@Override
			public void run() {
				ContentResolver resolver = mContext.getContentResolver();
				Uri ret = null;
				try {
					ret = resolver.insert(StaticDataContentProvider.sNewUrl, bean.getContentValues());
					if (ret != null) {
						bean.setFromDB(true);
					}
					if (UtilTool.isEnableLog()) {
						UtilTool.logStatic("Insert static Data to DB:"
								+ bean.getContentValues().get(
										DataBaseHelper.TABLE_STATISTICS_COLOUM_DATA));
					}
				} catch (Exception e) {
//					try {
						mCanNotFindUrl = true;
//						long id = getDataHelper().insert(DataBaseHelper.TABLE_STATISTICS_NEW, bean.getContentValues());
//						if (id != -1) {
//							bean.setFromDB(true);
//						}
//					} catch (Exception e1) {
//						UtilTool.printException(e1);
//					}
				}
			}
		});
		try {
			if (!mSingleExecutor.isShutdown()) {
				mSingleExecutor.execute(task);
			}
		} catch (Exception e) {
			UtilTool.printException(e);
		}
	}

	public void insertCtrlInfoAsync(final Map<String, CtrlBean> ctrlBeanMap, AsyncCallBack callBack) {
		DBAsyncTask task = new DBAsyncTask();
		task.addCallBack(callBack);
		task.addTask(new Runnable() {
			@Override
			public void run() {
				for (String key : ctrlBeanMap.keySet()) {
					CtrlBean bean = ctrlBeanMap.get(key);
					ContentResolver resolver = mContext.getContentResolver();
					try {
						resolver.insert(StaticDataContentProvider.sCtrlInfoUrl, bean.getContentValues());
					} catch (Exception e) {
//						try {
							mCanNotFindUrl = true;
//							getDataHelper().insert(DataBaseHelper.TABLE_CTRLINFO, bean.getContentValues());
//						} catch (Exception e1) {
//							UtilTool.printException(e1);
//						}
					}
				}
			}
		});
		try {
			if (!mSingleExecutor.isShutdown()) {
				mSingleExecutor.execute(task);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			UtilTool.printException(e);
		}

	}

	// public void insertPostData(final PostBean bean) {
	// ContentResolver resolver = mContext.getContentResolver();
	// Uri ret = null;
	// try {
	// ret = resolver.insert(StaticDataContentProvider.URL,
	// bean.getContentValues());
	// if (ret != null) {
	// bean.setFromDB(true);
	// }
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// try {
	// mCanNotFindUrl = true;
	// long id = getDataHelper().insert(
	// DataBaseHelper.TABLE_STATISTICS,
	// bean.getContentValues());
	// if (id != -1) {
	// bean.setFromDB(true);
	// }
	// } catch (Exception e1) {
	// // TODO Auto-generated catch block
	// UtilTool.printException(e1);
	// }
	// }
	//
	// }

	/**
	 * 查询老版本（Version < 1.10）SDK数据库中的数据
	 * 
	 * @return
	 */
	public LinkedList<PostBean> queryOldSDKVersionData() {
		Cursor cursor = null;
		LinkedList<PostBean> list = null;
		try {
			ContentResolver resolver = mContext.getContentResolver();
			cursor = resolver.query(StaticDataContentProvider.sUrl, null, null, null, null);
			if (cursor != null) {
				list = new LinkedList<PostBean>();
				cursor.moveToPosition(-1);
				while (cursor.moveToNext()) {
					PostBean bean = new PostBean();
					bean.parse(cursor);
					list.add(bean);
				}

			} else if (mCanNotFindUrl && cursor == null) {
				throw new IllegalArgumentException("Unknown URL" + StaticDataContentProvider.sUrl);
			}
		} catch (Exception e) {
			// cursor = getDataHelper().query(DataBaseHelper.TABLE_STATISTICS,
			// null, null, null, null);
			// try {
			// if (cursor != null) {
			// list = new LinkedList<PostBean>();
			// cursor.moveToPosition(-1);
			// while (cursor.moveToNext()) {
			// PostBean bean = new PostBean();
			// bean.parse(cursor);
			// list.add(bean);
			// }
			// }
			// UtilTool.printException(e);
			// } catch (Exception e1) {
			// // TODO Auto-generated catch block
			// UtilTool.printException(e1);
			// }
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public Map<String, CtrlBean> queryCtrlInfo() {
		Map<String, CtrlBean> ctrlMap = new HashMap<String, CtrlBean>();
		Cursor cursor = null;
		try {
			ContentResolver resolver = mContext.getContentResolver();
			cursor = resolver.query(StaticDataContentProvider.sCtrlInfoUrl, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToPosition(-1);
				while (cursor.moveToNext()) {
					CtrlBean bean = new CtrlBean(cursor.getLong(cursor
							.getColumnIndex(DataBaseHelper.TABLE_CTRLINFO_COLOUM_VALIDTIME)), cursor.getLong(cursor
							.getColumnIndex(DataBaseHelper.TABLE_CTRLINFO_COLOUM_INTERVALTIME)),
							cursor.getString(cursor.getColumnIndex(DataBaseHelper.TABLE_CTRLINFO_COLOUM_BN)),
							cursor.getString(cursor.getColumnIndex(DataBaseHelper.TABLE_CTRLINFO_COLOUM_UPDATETIME)),
							cursor.getInt(cursor.getColumnIndex(DataBaseHelper.TABLE_CTRLINFO_COLOUM_FUNID)),
							cursor.getLong(cursor.getColumnIndex(DataBaseHelper.TABLE_CTRLINFO_COLOUM_STARTIME)),
							cursor.getInt(cursor.getColumnIndex(DataBaseHelper.TABLE_CTRLINFO_COLOUM_NETWORK)),
							cursor.getInt(cursor.getColumnIndex(DataBaseHelper.TABLE_CTRLINFO_COLOUM_PRIORITY)));
					ctrlMap.put(String.valueOf(bean.getFunID()), bean);
				}
			}
		} catch (Exception e) {
			UtilTool.printException(e);
//			try {
//				cursor = getDataHelper().query(DataBaseHelper.TABLE_CTRLINFO, null, null, null, null);
//				if (cursor != null && cursor.getCount() > 0) {
//					cursor.moveToPosition(-1);
//					while (cursor.moveToNext()) {
//						CtrlBean bean = new CtrlBean(
//								cursor.getLong(cursor.getColumnIndex(DataBaseHelper.TABLE_CTRLINFO_COLOUM_VALIDTIME)),
//								cursor.getLong(cursor.getColumnIndex(DataBaseHelper.TABLE_CTRLINFO_COLOUM_INTERVALTIME)),
//								cursor.getString(cursor.getColumnIndex(DataBaseHelper.TABLE_CTRLINFO_COLOUM_BN)),
//								cursor.getString(cursor.getColumnIndex(DataBaseHelper.TABLE_CTRLINFO_COLOUM_UPDATETIME)),
//								cursor.getInt(cursor.getColumnIndex(DataBaseHelper.TABLE_CTRLINFO_COLOUM_FUNID)),
//								cursor.getLong(cursor.getColumnIndex(DataBaseHelper.TABLE_CTRLINFO_COLOUM_STARTIME)),
//								cursor.getInt(cursor.getColumnIndex(DataBaseHelper.TABLE_CTRLINFO_COLOUM_NETWORK)),
//								cursor.getInt(cursor.getColumnIndex(DataBaseHelper.TABLE_CTRLINFO_COLOUM_PRIORITY)));
//						ctrlMap.put(String.valueOf(bean.getFunID()), bean);
//					}
//				}
//			} catch (Exception e1) {
//				UtilTool.printException(e1);
//			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return ctrlMap;
	}

	public LinkedList<PostBean> queryPostDatas(HashSet<String> funidList) {
		Cursor cursor = null;
		LinkedList<PostBean> list = new LinkedList<PostBean>();
		StringBuffer where = null;
		if (funidList != null && funidList.size() > 0) {
			where = new StringBuffer("funid IN (");
			for (String funid : funidList) {
				where.append(funid + ",");
			}
			where.deleteCharAt(where.length() - 1);
			where.append(")");
		}

		if (where == null) {
			return list;
		}
		try {
			ContentResolver resolver = mContext.getContentResolver();
			cursor = resolver.query(StaticDataContentProvider.sNewUrl, null, where.toString(), null,
					DataBaseHelper.TABLE_STATISTICS_COLOUM_ID + " DESC limit " + QUERYLIMIT);
			if (cursor != null) {
				cursor.moveToPosition(-1);
				while (cursor.moveToNext()) {
					PostBean bean = new PostBean();
					bean.parse(cursor);
					list.add(bean);
				}
				if (UtilTool.isEnableLog()) {
					UtilTool.logStatic("Query post data:" + where.toString() + ",data count:"
							+ cursor.getCount());
				}
			} else if (mCanNotFindUrl && cursor == null) {
				throw new IllegalArgumentException("Unknown URL" + StaticDataContentProvider.sNewUrl);
			}
		} catch (Exception e) {
			UtilTool.printException(e);
//			cursor = getDataHelper().query(DataBaseHelper.TABLE_STATISTICS_NEW, null, where.toString(), null,
//					DataBaseHelper.TABLE_STATISTICS_COLOUM_ID + " DESC limit " + QUERYLIMIT);
//			try {
//				if (cursor != null && cursor.getCount() > 0) {
//					list = new LinkedList<PostBean>();
//					cursor.moveToPosition(-1);
//					while (cursor.moveToNext()) {
//						PostBean bean = new PostBean();
//						bean.parse(cursor);
//						list.add(bean);
//					}
//				}
//				UtilTool.printException(e);
//			} catch (Exception e1) {
//				UtilTool.printException(e1);
//			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	private static final int QUERYLIMIT = 300;

	/**
	 * 查询数据库中未上传但是急需上传的数据（isold = 1）
	 * 
	 * @return
	 */
	public LinkedList<PostBean> queryOldData() {
		Cursor cursor = null;
		LinkedList<PostBean> list = new LinkedList<PostBean>();
		try {
			ContentResolver resolver = mContext.getContentResolver();
			cursor = resolver.query(StaticDataContentProvider.sNewUrl, null, "isold=1", null,
					DataBaseHelper.TABLE_STATISTICS_COLOUM_ID + " DESC limit " + QUERYLIMIT);
			if (cursor != null) {
				if (UtilTool.isEnableLog()) {
					UtilTool.logStatic("Query all old data, data count:" + cursor.getCount());
				}
				cursor.moveToPosition(-1);
				while (cursor.moveToNext()) {
					PostBean bean = new PostBean();
					bean.parse(cursor);
					list.add(bean);
				}
			} else if (mCanNotFindUrl && cursor == null) {
				throw new IllegalArgumentException("Unknown URL" + StaticDataContentProvider.sNewUrl);
			}
		} catch (Exception e) {
			// TODO: handle exception
			// cursor =
			// getDataHelper().query(DataBaseHelper.TABLE_STATISTICS_NEW, null,
			// "isold=1", null,
			// DataBaseHelper.TABLE_STATISTICS_COLOUM_ID + " DESC limit " +
			// QUERYLIMIT);
			// try {
			// if (cursor != null && cursor.getCount() > 0) {
			// list = new LinkedList<PostBean>();
			// cursor.moveToPosition(-1);
			// while (cursor.moveToNext()) {
			// PostBean bean = new PostBean();
			// bean.parse(cursor);
			// list.add(bean);
			// }
			// }
			// UtilTool.printException(e);
			// } catch (Exception e1) {
			// // TODO Auto-generated catch block
			// UtilTool.printException(e1);
			// }
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public LinkedList<PostBean> queryAllData() {
		Cursor cursor = null;
		LinkedList<PostBean> list = new LinkedList<PostBean>();
		try {
			ContentResolver resolver = mContext.getContentResolver();
			cursor = resolver.query(StaticDataContentProvider.sNewUrl, null, null, null,
					DataBaseHelper.TABLE_STATISTICS_COLOUM_ID + " DESC");
			if (cursor != null) {
				if (UtilTool.isEnableLog()) {
					UtilTool.logStatic("Query all data in db, data count:" + cursor.getCount());
				}
				cursor.moveToPosition(-1);
				while (cursor.moveToNext()) {
					PostBean bean = new PostBean();
					bean.parse(cursor);
					list.add(bean);
				}
			} else if (mCanNotFindUrl && cursor == null) {
				throw new IllegalArgumentException("Unknown URL" + StaticDataContentProvider.sNewUrl);
			}
		} catch (Exception e) {
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return list;
	}

	public int queryDataCount() {
		Cursor cursor = null;
		int count = 0;
		try {
			ContentResolver resolver = mContext.getContentResolver();
			cursor = resolver.query(StaticDataContentProvider.sNewUrl, null, null, null, null);
			if (cursor != null) {
				count = cursor.getCount();
			} else if (mCanNotFindUrl && cursor == null) {
				throw new IllegalArgumentException("Unknown URL" + StaticDataContentProvider.sNewUrl);
			}
		} catch (Exception e) {
			// TODO: handle exception
			// cursor =
			// getDataHelper().query(DataBaseHelper.TABLE_STATISTICS_NEW, null,
			// null, null, null);
			// try {
			// if (cursor != null && cursor.getCount() > 0) {
			// count = cursor.getCount();
			// }
			// UtilTool.printException(e);
			// } catch (Exception e1) {
			// // TODO Auto-generated catch block
			// UtilTool.printException(e1);
			// }
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return count;
	}

	public LinkedList<PostBean> queryPostDatas(String funid) {
		Cursor cursor = null;
		LinkedList<PostBean> list = null;

		try {
			ContentResolver resolver = mContext.getContentResolver();
			cursor = resolver.query(StaticDataContentProvider.sNewUrl, null, "funid IN (" + funid + ")", null,
					DataBaseHelper.TABLE_STATISTICS_COLOUM_ID + " DESC limit " + QUERYLIMIT);

			if (cursor != null) {
				if (UtilTool.isEnableLog()) {
					UtilTool.logStatic("Query Post Data In funid:" + funid + " and data Count:"
							+ cursor.getCount());
				}
				list = new LinkedList<PostBean>();
				cursor.moveToPosition(-1);
				while (cursor.moveToNext()) {
					PostBean bean = new PostBean();
					bean.parse(cursor);
					list.add(bean);
				}

			} else if (mCanNotFindUrl && cursor == null) {
				throw new IllegalArgumentException("Unknown URL" + StaticDataContentProvider.sNewUrl);
			}
		} catch (Exception e) {
			UtilTool.printException(e);

			// cursor =
			// getDataHelper().query(DataBaseHelper.TABLE_STATISTICS_NEW, null,
			// "funid IN (" + funid + ")", null,
			// DataBaseHelper.TABLE_STATISTICS_COLOUM_ID + " DESC limit " +
			// QUERYLIMIT);
			// if (cursor != null && cursor.getCount() > 0) {
			// list = new LinkedList<PostBean>();
			// cursor.moveToPosition(-1);
			// while (cursor.moveToNext()) {
			// PostBean bean = new PostBean();
			// bean.parse(cursor);
			// list.add(bean);
			// }
			// }
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return list;
	}

	public void deletePushData(PostBean bean) {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder();
		PostBean tmp = bean;
		builder.append("(");
		int beanCount = 0;
		while (tmp != null) {
			beanCount++;
			builder.append("'");
			builder.append(tmp.mId);
			builder.append("'");
			if (tmp.mNext != null) {
				builder.append(",");
			}
			tmp = tmp.mNext;
		}
		builder.append(")");
		String where = null;
		if (beanCount > 1) {
			where = "funid=" + bean.mFunId + " and " + DataBaseHelper.TABLE_STATISTICS_COLOUM_ID + " IN "
					+ builder.toString();
		} else {
			where = "funid=" + bean.mFunId + " and " + DataBaseHelper.TABLE_STATISTICS_COLOUM_ID + "='" + bean.mId
					+ "'";
		}
		try {
			ContentResolver resolver = mContext.getContentResolver();
			int count = resolver.delete(StaticDataContentProvider.sNewUrl, where, null);
			if (UtilTool.isEnableLog()) {
				UtilTool.log(null, "deletePushData from db count:" + count + ",where:" + where);
			}
		} catch (Exception e) {
			UtilTool.printException(e);
//			try {
//				getDataHelper().delete(DataBaseHelper.TABLE_STATISTICS_NEW, where, null);
//			} catch (Exception e1) {
//				// TODO Auto-generated catch block
//				UtilTool.printException(e1);
//			}
		}
	}

	private synchronized void closeDB() {
		if (mHelp != null) {
			mHelp.close();
		}
	}

	public void destory() {
		try {
			mSingleExecutor.shutdown();
			closeDB();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			UtilTool.printException(e);
		}
	}

	public void setDataOld(PostBean bean) {
		PostBean tmp = bean.mNext;
		StringBuffer buffer = new StringBuffer();
		buffer.append("'" + bean.mId + "',");
		while (tmp != null) {
			buffer.append("'" + tmp.mId + "',");
			tmp = tmp.mNext;
		}
		String where = "";
		buffer.deleteCharAt(buffer.length() - 1);
		where = buffer.toString();

		ContentValues contentValues = new ContentValues();
		contentValues.put(DataBaseHelper.TABLE_STATISTICS_COLOUM_ISOLD, true);
		try {
			ContentResolver resolver = mContext.getContentResolver();
			int count = resolver.update(StaticDataContentProvider.sNewUrl, contentValues, "id IN ("
					+ where + ")", null);
			if (UtilTool.isEnableLog()) {
				UtilTool.log(null, "setDataOld in db count:" + count);
			}
		} catch (Exception e) {
			UtilTool.printException(e);
//			try {
//				getDataHelper().update(DataBaseHelper.TABLE_STATISTICS_NEW, contentValues, "id IN (" + where + ")",
//						null);
//			} catch (Exception e1) {
//				// TODO Auto-generated catch block
//				UtilTool.printException(e1);
//			}
		}
	}

	/**
	 * 将所有现有数据库中的数据的isold字段设置为true
	 */
	public int setAllDataOld() {
		ContentValues contentValues = new ContentValues();
		contentValues.put(DataBaseHelper.TABLE_STATISTICS_COLOUM_ISOLD, true);
		int count = 0;
		try {
			ContentResolver resolver = mContext.getContentResolver();
			count = resolver.update(StaticDataContentProvider.sNewUrl, contentValues, "isold=0",
					null);
			if (UtilTool.isEnableLog()) {
				UtilTool.logStatic("Set Data new to old,success count:" + count);
			}
		} catch (Exception e) {
			UtilTool.printException(e);
//			try {
//				count = getDataHelper().update(DataBaseHelper.TABLE_STATISTICS_NEW, contentValues, null, null);
//			} catch (Exception e1) {
//				UtilTool.printException(e1);
//			}
		}
		return count;
	}

	public int deleteOldCtrlInfo() {
		int count = 0;
		try {
			ContentResolver resolver = mContext.getContentResolver();
			count = resolver.delete(StaticDataContentProvider.sCtrlInfoUrl, null, null);
			if (UtilTool.isEnableLog()) {
				UtilTool.logStatic("Delete old ctrlInfo from db, ctrlInfo count:" + count);
			}
		} catch (Exception e) {
			UtilTool.printException(e);
//			try {
//				count = getDataHelper().delete(DataBaseHelper.TABLE_CTRLINFO, null, null);
//			} catch (Exception e1) {
//				// TODO Auto-generated catch block
//				UtilTool.printException(e1);
//			}
		}
		return count;
	}

	public PostBean queryPostData(String stringExtra) {
		Cursor cursor = null;
		PostBean bean = null;

		try {
			ContentResolver resolver = mContext.getContentResolver();

			cursor = resolver.query(StaticDataContentProvider.sNewUrl, null, "id IN ('" + stringExtra + "')",
					null, null);

			if (cursor != null && cursor.getCount() > 0) {
				if (UtilTool.isEnableLog()) {
					UtilTool.logStatic("Query Post Data In id:" + stringExtra + " and data Count:"
							+ cursor.getCount());
				}
				cursor.moveToPosition(0);

				bean = new PostBean();
				bean.parse(cursor);
				if (UtilTool.isEnableLog()) {
					UtilTool.logStatic("beanData:" + bean.mData);
				}
			} else if (mCanNotFindUrl && cursor == null) {
				throw new IllegalArgumentException("Unknown URL" + StaticDataContentProvider.sNewUrl);
			}
		} catch (Exception e) {
			UtilTool.printException(e);
			// cursor =
			// getDataHelper().query(DataBaseHelper.TABLE_STATISTICS_NEW, null,
			// "funid IN ('" + stringExtra + "')", null, null);
			// try {
			// if (cursor != null && cursor.getCount() > 0) {
			// cursor.moveToPosition(0);
			// bean = new PostBean();
			// bean.parse(cursor);
			// }
			// UtilTool.printException(e);
			//
			// } catch (Exception e1) {
			// // TODO Auto-generated catch block
			// UtilTool.printException(e1);
			// }
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return bean;
	}

	public void deleteOldData(LinkedList<PostBean> bean) {
		StringBuffer where = new StringBuffer("id IN (");
		for (PostBean postBean : bean) {
			where.append("" + postBean.mId + ",");
		}
		where.deleteCharAt(where.length() - 1);
		where.append(")");
		try {
			ContentResolver resolver = mContext.getContentResolver();
			int count = resolver.delete(StaticDataContentProvider.sUrl, where.toString(), null);
			if (UtilTool.isEnableLog()) {
				UtilTool.log(null, "Delete old data from db and where: " + where.toString()
						+ " and count:" + count);
			}
		} catch (Exception e) {
			UtilTool.printException(e);
//			try {
//				getDataHelper().delete(DataBaseHelper.TABLE_STATISTICS, where.toString(), null);
//			} catch (Exception e1) {
//				UtilTool.printException(e1);
//			}
		}
	}

}
