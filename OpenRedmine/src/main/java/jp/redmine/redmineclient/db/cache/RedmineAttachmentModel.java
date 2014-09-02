package jp.redmine.redmineclient.db.cache;

import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.StatementBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import jp.redmine.redmineclient.BuildConfig;
import jp.redmine.redmineclient.entity.RedmineAttachment;
import jp.redmine.redmineclient.entity.RedmineAttachmentData;


public class RedmineAttachmentModel {
	private final static String TAG = RedmineAttachmentModel.class.getSimpleName();
	protected Dao<RedmineAttachment, Long> dao;
	protected Dao<RedmineAttachmentData, Long> daoData;
	public RedmineAttachmentModel(DatabaseCacheHelper helper) {
		try {
			dao = helper.getDao(RedmineAttachment.class);
			daoData = helper.getDao(RedmineAttachmentData.class);
		} catch (SQLException e) {
			Log.e(TAG,TAG,e);
		}
	}

	public RedmineAttachment fetchById(int connection, int journalId) throws SQLException{
		PreparedQuery<RedmineAttachment> query = dao.queryBuilder().where()
		.eq(RedmineAttachment.CONNECTION, connection)
		.and()
		.eq(RedmineAttachment.ATTACHMENT_ID, journalId)
		.prepare();
		Log.d(TAG,query.getStatement());
		RedmineAttachment item = dao.queryForFirst(query);
		if(item == null)
			item = new RedmineAttachment();
		return item;
	}

	public RedmineAttachment fetchById(long id) throws SQLException{
		RedmineAttachment item = dao.queryForId(id);
		if(item == null)
			item = new RedmineAttachment();
		return item;
	}

	public int insert(RedmineAttachment item) throws SQLException{
		int count = dao.create(item);
		return count;
	}

	public int update(RedmineAttachment item) throws SQLException{
		int count = dao.update(item);
		return count;
	}
	public int delete(RedmineAttachment item) throws SQLException{
		int count = dao.delete(item);
		return count;
	}
	public int delete(long id) throws SQLException{
		int count = dao.deleteById(id);
		return count;
	}

	protected <X extends StatementBuilder<T,A>, T,A> X setupWhere(X builder, RedmineAttachment attachment) throws SQLException {
		builder.where()
				.eq(RedmineAttachmentData.CONNECTION, attachment.getConnectionId())
				.and()
				.eq(RedmineAttachmentData.ATTACHMENT_ID, attachment.getAttachmentId())
		;
		return builder;
	}
	public long saveData(RedmineAttachment attachment, InputStream stream) throws IOException, SQLException {
		stream =  new GZIPInputStream(stream);
		byte buffer[] = new byte[1024*128];
		int count;
		int deleted = setupWhere(daoData.deleteBuilder(),attachment).delete();
		if (BuildConfig.DEBUG) Log.i(TAG,"deleted " + deleted + " rows");
		long total_size = 0;
		while((count = stream.read(buffer)) != -1){
			RedmineAttachmentData data = new RedmineAttachmentData();
			data.setAttachemnt(attachment);
			data.setData(buffer);
			data.setSize(count);
			daoData.create(data);
			total_size += count;
		}
		stream.close();
		if (BuildConfig.DEBUG) Log.i(TAG,"inserted  " + total_size + " rows");
		return total_size;
	}
	public boolean exportToFile(RedmineAttachment attachment, File file) throws IOException, SQLException {
		if (setupWhere(daoData.queryBuilder(),attachment).countOf() < 1)
			return false;
		OutputStream output = new FileOutputStream(file);
		loadData(attachment, output);
		output.close();
		return true;
	}
	public long loadData(RedmineAttachment attachment, OutputStream stream) throws IOException, SQLException {
		stream =  new GZIPOutputStream(stream);
		long total_size = 0;
		for(RedmineAttachmentData data :
				setupWhere(daoData.queryBuilder(),attachment)
				.orderBy(RedmineAttachmentData.ID, true)
				.query()
		){
			stream.write(data.getData(),0, data.getSize());
			total_size += data.getSize();
		}
		stream.flush();
		stream.close();
		if (BuildConfig.DEBUG) Log.i(TAG,"selected  " + total_size + " bytes");
		return total_size;
	}

	public RedmineAttachment refreshItem(int connection_id,RedmineAttachment data) throws SQLException{
		if(data == null)
			return null;

		RedmineAttachment project = this.fetchById(connection_id, data.getAttachmentId());
		data.setConnectionId(connection_id);

		if(project.getId() == null){
			this.insert(data);
		} else {
			data.setId(project.getId());

			if(project.getModified() == null){
				project.setModified(new java.util.Date());
			}
			if(data.getModified() == null){
				data.setModified(new java.util.Date());
			}
			if(!project.getModified().before(data.getModified())){
				this.update(data);
			}
		}
		return data;
	}

	public RedmineAttachment refreshItem(RedmineAttachment journal) throws SQLException {
		return refreshItem(journal.getConnectionId(), journal);
	}
}
