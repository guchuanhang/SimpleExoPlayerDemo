package com.google.android.exoplayer.demo.com.google.android.exoplayer.bean;

/**
 * 电台表
 * 
 * @author Chuanhang.Gu
 * 
 */
public class MediaField {
	public static final String tableName = "D_Media";
	public static final String rowKey = "RowKey";
	/**
	 * Icon 媒体图片GUID
	 */
	public static final String icon = "Icon";
	/**
	 * 	Name	电台名称
	 */
	public static final String name = "Name";
	public static final String crdate = "F_CRDATE";
	public static final String chdate = "F_CHDATE";
	/*
	 * 	MediaFM	媒体调频，如：101.1

	 */
	public static final String FM = "MediaFM";
	/**
	 * 	MediaTag	媒体的标签，如：经济

	 */
	public static final String tag = "MediaTag";
	/**
	 * 	MediaUrl	流媒体地址

	 */
	public static final String url = "MediaUrl";
	/**
	 * 	MediaType	所述媒体类型，0：本地电台 1：广播电台 2：校园电台

	 */
	public static final String type = "MediaType";
	public static final String type_local = "0";
	public static final String type_broadcast = "1";
	public static final String type_school = "2";
	/**
	 * 	NumberOfListener	听众数

	 */
	public static final String listener = "NumberOfListener";
	/**
	 * 	CurrentProgram	当前播出节目

	 */
	public static final String currentProgram = "CurrentProgram";
	/**
	CurrentProgramId	当前播出节目id
	 * 
	 */
	public static final String currentProgramId = "CurrentProgramId";
	/**
	 * 
	ProgramTimePeriod	当前节目播出时段，取自D_Program中F_begintime-F_endtime
	 */
	public static final String peroid = "ProgramTimePeriod";
	/**
	Del	0:未删除 1：已删除
	 * 
	 */
	public static final String del = "Del";
	public static final String del_no = "0";
	public static final String del_yes = "1";
	/**
	 * 
	Notes	备注
	 */
	public static final String notes = "Notes";
	/**
	 * Show 0:显示 1:不显示
	 * 
	 */
	public static final String show = "Show";
	public static final String show_no = "1";
	public static final String show_yes = "0";
	/**
	 * 厚建系统提供的直播媒体id，为了保证旧版的app能使用所以单独建立一个字段。
	 */
	public static final String mediaId = "MediaId";
	/**
	 * 当前直播流的时间戳。因为直播流只有两个小时有效期，需要根据该时间进行定期更新。
	 */
	public static final String mediaTimestamp = "MediaTimestamp";
	/**
	 * 当前直播流的地址
	 */
	public static final String mediaDynamicUrl = "MediaDynamicUrl";
}
