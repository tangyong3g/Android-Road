package com.graphics.engine.gl.view;


/**
 * com.android.internal.R.styleable 里面的常量映射
 * @author dengweiming
 * 
 * @hide
 */
// CHECKSTYLE:OFF
public class com_android_internal_R_styleable {
	/*
	 * 从这个网页（或docs目录下）中拿到单个常量的记录：
	 * http://www.kiwidoc.com/java/l/x/android/android/8/p/com.android.internal/c/R.styleable
	 * 例如：
	 * public static final int View_scrollbarSize = 0
	 * 那么对应的
	 * public static final int[] View 里面的第0个元素的值就为 android.R.attr.scrollbarSize = 16842851。
	 * 实际上要获取这个数组，可以在调试时使用到它的时候进入obtainStyledAttributes函数，看参数的值来得到，例如：
	 * TypedArray a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.View,
                defStyle, 0);
	 */
	
	//======================= View =======================
	public static final int[] View = {
		16842851, 16842852, 16842853, 16842854, 16842855, 
		16842856, 16842857, 16842879, 16842960, 16842961, 
		16842962, 16842963, 16842964, 16842965, 16842966, 
		16842967, 16842968, 16842969, 16842970, 16842971, 
		16842972, 16842973, 16842974, 16842975, 16842976, 
		16842977, 16842978, 16842979, 16842980, 16842981, 
		16842982, 16842983, 16842984, 16842985, 16843071, 
		16843072, 16843285, 16843286, 16843342, 16843358, 
		16843375, 16843379, 16843432, 16843433, 16843434, 
	};	
	public static final int View_background = 12;
	public static final int View_clickable = 29;
	public static final int View_contentDescription = 41;
	public static final int View_drawingCacheQuality = 32;
	public static final int View_duplicateParentState = 33;
	public static final int View_fadeScrollbars = 44;
	public static final int View_fadingEdge = 23;
	public static final int View_fadingEdgeLength = 24;
	public static final int View_fitsSystemWindows = 21;
	public static final int View_focusable = 18;
	public static final int View_focusableInTouchMode = 19;
	public static final int View_hapticFeedbackEnabled = 39;
	public static final int View_id = 8;
	public static final int View_isScrollContainer = 38;
	public static final int View_keepScreenOn = 37;
	public static final int View_longClickable = 30;
	public static final int View_minHeight = 35;
	public static final int View_minWidth = 34;
	public static final int View_nextFocusDown = 28;
	public static final int View_nextFocusLeft = 25;
	public static final int View_nextFocusRight = 26;
	public static final int View_nextFocusUp = 27;
	public static final int View_onClick = 40;
	public static final int View_padding = 13;
	public static final int View_paddingBottom = 17;
	public static final int View_paddingLeft = 14;
	public static final int View_paddingRight = 16;
	public static final int View_paddingTop = 15;
	public static final int View_saveEnabled = 31;
	public static final int View_scrollX = 10;
	public static final int View_scrollY = 11;
	public static final int View_scrollbarAlwaysDrawHorizontalTrack = 5;
	public static final int View_scrollbarAlwaysDrawVerticalTrack = 6;
	public static final int View_scrollbarDefaultDelayBeforeFade = 43;
	public static final int View_scrollbarFadeDuration = 42;
	public static final int View_scrollbarSize = 0;
	public static final int View_scrollbarStyle = 7;
	public static final int View_scrollbarThumbHorizontal = 1;
	public static final int View_scrollbarThumbVertical = 2;
	public static final int View_scrollbarTrackHorizontal = 3;
	public static final int View_scrollbarTrackVertical = 4;
	public static final int View_scrollbars = 22;
	public static final int View_soundEffectsEnabled = 36;
	public static final int View_tag = 9;
	public static final int View_visibility = 20;
	
	
	//======================= ViewGroup =======================
	 public static final int[] ViewGroup = {
		 16842986, 16842987, 16842988, 16842989, 16842990, 
		 16842991, 16842992, 16842993, 
	 };
	 public static final int ViewGroup_addStatesFromChildren = 6;
	 public static final int ViewGroup_alwaysDrawnWithCache = 5;
	 public static final int ViewGroup_animationCache = 3;
	 public static final int ViewGroup_clipChildren = 0;
	 public static final int ViewGroup_clipToPadding = 1;
	 public static final int ViewGroup_descendantFocusability = 7;
	 public static final int ViewGroup_layoutAnimation = 2;
	 public static final int ViewGroup_persistentDrawingCache = 4;
	 
	 
	 
	//======================= FrameLayout =======================
	public static final int[] FrameLayout = {
		16843017, 16843018, 16843264, 16843459, 
	};
	public static final int FrameLayout_foreground = 0;
	public static final int FrameLayout_foregroundGravity = 2;
	public static final int FrameLayout_foregroundInsidePadding = 3;
	public static final int FrameLayout_measureAllChildren = 1;
	
	//FrameLayout.LayoutParams 因为我们直接使用了这个类，所以不需要访问这些常量
	public static final int[] FrameLayout_Layout = {
		16842931, 
	};
	public static final int FrameLayout_Layout_layout_gravity = 0;
	  
	
	
	//======================= LinearLayout =======================
	public static final int[] LinearLayout = {
		16842927, 16842948, 16843046, 16843047, 16843048, 
		16843460, 
	};
	public static final int LinearLayout_baselineAligned = 2;
	public static final int LinearLayout_baselineAlignedChildIndex = 3;
	public static final int LinearLayout_gravity = 0;
	public static final int LinearLayout_orientation = 1;
	public static final int LinearLayout_useLargestChild = 5;
	public static final int LinearLayout_weightSum = 4;

	//LinearLayout.LayoutParams 因为我们直接使用了这个类，所以不需要访问这些常量
	public static final int[] LinearLayout_Layout = {
		16842931, 16842996, 16842997, 16843137, 
	};	
	public static final int LinearLayout_Layout_layout_gravity = 0;
	public static final int LinearLayout_Layout_layout_height = 2;
	public static final int LinearLayout_Layout_layout_weight = 3;
	public static final int LinearLayout_Layout_layout_width = 1;
	
	
	
	//======================= RelativeLayout =======================
	public static final int[] RelativeLayout = {
		16842927, 16843263, 
	};
	public static final int RelativeLayout_gravity = 0;
	public static final int RelativeLayout_ignoreGravity = 1;
	
	//RelativeLayout.LayoutParams
	public static final int[] RelativeLayout_Layout = {
		16843138, 16843139, 16843140, 16843141, 16843142, 
		16843143, 16843144, 16843145, 16843146, 16843147, 
		16843148, 16843149, 16843150, 16843151, 16843152, 
		16843153, 16843154, 
	};

	 public static final int RelativeLayout_Layout_layout_above = 2;
	 public static final int RelativeLayout_Layout_layout_alignBaseline = 4;
	 public static final int RelativeLayout_Layout_layout_alignBottom = 8;
	 public static final int RelativeLayout_Layout_layout_alignLeft = 5;
	 public static final int RelativeLayout_Layout_layout_alignParentBottom = 12;
	 public static final int RelativeLayout_Layout_layout_alignParentLeft = 9;
	 public static final int RelativeLayout_Layout_layout_alignParentRight = 11;
	 public static final int RelativeLayout_Layout_layout_alignParentTop = 10;
	 public static final int RelativeLayout_Layout_layout_alignRight = 7;
	 public static final int RelativeLayout_Layout_layout_alignTop = 6;
	 public static final int RelativeLayout_Layout_layout_alignWithParentIfMissing = 16;
	 public static final int RelativeLayout_Layout_layout_below = 3;
	 public static final int RelativeLayout_Layout_layout_centerHorizontal = 14;
	 public static final int RelativeLayout_Layout_layout_centerInParent = 13;
	 public static final int RelativeLayout_Layout_layout_centerVertical = 15;
	 public static final int RelativeLayout_Layout_layout_toLeftOf = 0;
	 public static final int RelativeLayout_Layout_layout_toRightOf = 1;
	 
	 
	
	 //======================= ViewDrawableStates =======================
	 public static final int[] ViewDrawableStates = {
		 android.R.attr.state_focused, 
		 android.R.attr.state_window_focused, 
		 android.R.attr.state_enabled, 
		 android.R.attr.state_selected, 
		 android.R.attr.state_pressed, 
	 };


		 
	//======================= TextView =======================
	public static final int[] TextView = {
		 16842766, 16842804, 16842901, 16842902, 16842903, 
		 16842904, 16842905, 16842906, 16842907, 16842923, 
		 16842927, 16842928, 16842929, 16843039, 16843040, 
		 16843071, 16843072, 16843086, 16843087, 16843088, 
		 16843089, 16843090, 16843091, 16843092, 16843093, 
		 16843094, 16843095, 16843096, 16843097, 16843098, 
		 16843099, 16843100, 16843101, 16843102, 16843103, 
		 16843104, 16843105, 16843106, 16843107, 16843108, 
		 16843109, 16843110, 16843111, 16843112, 16843113, 
		 16843114, 16843115, 16843116, 16843117, 16843118, 
		 16843119, 16843120, 16843121, 16843287, 16843288, 
		 16843293, 16843296, 16843299, 16843300, 16843364, 
		 16843365, 16843366, 
	};

	public static final int TextView_autoLink = 11;
	@Deprecated
	public static final int TextView_autoText = 45;
	public static final int TextView_bufferType = 17;
	@Deprecated
	public static final int TextView_capitalize = 44;
	public static final int TextView_cursorVisible = 21;
	public static final int TextView_digits = 41;
	public static final int TextView_drawableBottom = 49;
	public static final int TextView_drawableLeft = 50;
	public static final int TextView_drawablePadding = 52;
	public static final int TextView_drawableRight = 51;
	public static final int TextView_drawableTop = 48;
	@Deprecated
	public static final int TextView_editable = 46;
	public static final int TextView_editorExtras = 58;
	public static final int TextView_ellipsize = 9;
	public static final int TextView_ems = 27;
	@Deprecated
	public static final int TextView_enabled = 0;
	public static final int TextView_freezesText = 47;
	public static final int TextView_gravity = 10;
	public static final int TextView_height = 24;
	public static final int TextView_hint = 19;
	public static final int TextView_imeActionId = 61;
	public static final int TextView_imeActionLabel = 60;
	public static final int TextView_imeOptions = 59;
	public static final int TextView_includeFontPadding = 34;
	@Deprecated
	public static final int TextView_inputMethod = 43;
	public static final int TextView_inputType = 56;
	public static final int TextView_lineSpacingExtra = 53;
	public static final int TextView_lineSpacingMultiplier = 54;
	public static final int TextView_lines = 23;
	public static final int TextView_linksClickable = 12;
	public static final int TextView_marqueeRepeatLimit = 55;
	public static final int TextView_maxEms = 26;
	public static final int TextView_maxHeight = 14;
	public static final int TextView_maxLength = 35;
	public static final int TextView_maxLines = 22;
	public static final int TextView_maxWidth = 13;
	public static final int TextView_minEms = 29;
	public static final int TextView_minHeight = 16;
	public static final int TextView_minLines = 25;
	public static final int TextView_minWidth = 15;
	@Deprecated
	public static final int TextView_numeric = 40;
	@Deprecated
	public static final int TextView_password = 31;
	@Deprecated
	public static final int TextView_phoneNumber = 42;
	public static final int TextView_privateImeOptions = 57;
	public static final int TextView_scrollHorizontally = 30;
	public static final int TextView_selectAllOnFocus = 33;
	public static final int TextView_shadowColor = 36;
	public static final int TextView_shadowDx = 37;
	public static final int TextView_shadowDy = 38;
	public static final int TextView_shadowRadius = 39;
	@Deprecated
	public static final int TextView_singleLine = 32;
	public static final int TextView_text = 18;
	public static final int TextView_textAppearance = 1;
	public static final int TextView_textColor = 5;
	public static final int TextView_textColorHighlight = 6;
	public static final int TextView_textColorHint = 7;
	public static final int TextView_textColorLink = 8;
	public static final int TextView_textScaleX = 20;
	public static final int TextView_textSize = 2;
	public static final int TextView_textStyle = 4;
	public static final int TextView_typeface = 3;
	public static final int TextView_width = 28;

	 

	// ======================= ImageView =======================
	public static final int[] ImageView = { 
		16843033, 16843037, 16843038, 16843039, 16843040, 
		16843041, 16843042, 16843043, 
	};

	public static final int ImageView_adjustViewBounds = 2;
	public static final int ImageView_baselineAlignBottom = 6;
	public static final int ImageView_cropToPadding = 7;
	public static final int ImageView_maxHeight = 4;
	public static final int ImageView_maxWidth = 3;
	public static final int ImageView_scaleType = 1;
	public static final int ImageView_src = 0;
	public static final int ImageView_tint = 5;
	

	
	// ======================= AbsListView =======================
	public static final int[] AbsListView = {
		16843003, 16843004, 16843005, 16843006, 16843007, 
		16843008, 16843009, 16843302, 16843313, 
	};

	public static final int AbsListView_cacheColorHint = 6;
	public static final int AbsListView_drawSelectorOnTop = 1;
	public static final int AbsListView_fastScrollEnabled = 7;
	public static final int AbsListView_listSelector = 0;
	public static final int AbsListView_scrollingCache = 3;
	public static final int AbsListView_smoothScrollbar = 8;
	public static final int AbsListView_stackFromBottom = 2;
	public static final int AbsListView_textFilterEnabled = 4;
	public static final int AbsListView_transcriptMode = 5;
	
	
	
	// ======================= ListView =======================
	public static final int[] ListView = {
		16842930, 16843049, 16843050, 16843051, 16843310, 
		16843311, 
	};

	public static final int ListView_choiceMode = 3;
	public static final int ListView_divider = 1;
	public static final int ListView_dividerHeight = 2;
	public static final int ListView_entries = 0;
	public static final int ListView_footerDividersEnabled = 5;
	public static final int ListView_headerDividersEnabled = 4;
	
	
	
	// ======================= GridView =======================
	public static final int[] GridView = {
		 16842927, 16843028, 16843029, 16843030, 16843031, 
		 16843032, 
	};

	public static final int GridView_columnWidth = 4;
	public static final int GridView_gravity = 0;
	public static final int GridView_horizontalSpacing = 1;
	public static final int GridView_numColumns = 5;
	public static final int GridView_stretchMode = 3;
	public static final int GridView_verticalSpacing = 2;
	
	
	
	// ======================= Animation =======================
	public static final int[] Animation = {
		16843073, 16843160, 16843196, 16843197, 16843198, 
		16843199, 16843200, 16843201, 16843343, 16843430, 
	};

	public static final int Animation_detachWallpaper = 9;
	public static final int Animation_duration = 1;
	public static final int Animation_fillAfter = 3;
	public static final int Animation_fillBefore = 2;
	public static final int Animation_fillEnabled = 8;
	public static final int Animation_interpolator = 0;
	public static final int Animation_repeatCount = 5;
	public static final int Animation_repeatMode = 6;
	public static final int Animation_startOffset = 4;
	public static final int Animation_zAdjustment = 7;



	// ======================= AnimationSet =======================
	public static final int[] AnimationSet = {
		16843195, 
	};
	public static final int AnimationSet_shareInterpolator = 0;



	// ======================= AlphaAnimation =======================
	public static final int[] AlphaAnimation = {
		16843210, 16843211, 
	};

	public static final int AlphaAnimation_fromAlpha = 0;
	public static final int AlphaAnimation_toAlpha = 1;


	
	// ======================= RotateAnimation =======================
	public static final int[] RotateAnimation = {
		16843187, 16843188, 16843189, 16843190, 
	};
	public static final int RotateAnimation_fromDegrees = 0;
	public static final int RotateAnimation_pivotX = 2;
	public static final int RotateAnimation_pivotY = 3;
	public static final int RotateAnimation_toDegrees = 1;



	// ======================= ScaleAnimation =======================
	public static final int[] ScaleAnimation = {
		16843189, 16843190, 16843202, 16843203, 16843204, 
		16843205, 
	};
	public static final int ScaleAnimation_fromXScale = 2;
	public static final int ScaleAnimation_fromYScale = 4;
	public static final int ScaleAnimation_pivotX = 0;
	public static final int ScaleAnimation_pivotY = 1;
	public static final int ScaleAnimation_toXScale = 3;
	public static final int ScaleAnimation_toYScale = 5;


	
	// ======================= TranslateAnimation =======================
	public static final int[] TranslateAnimation = {
		16843206, 16843207, 16843208, 16843209, 
	};
	public static final int TranslateAnimation_fromXDelta = 0;
	public static final int TranslateAnimation_fromYDelta = 2;
	public static final int TranslateAnimation_toXDelta = 1;
	public static final int TranslateAnimation_toYDelta = 3;
	
	
	
	// ======================= TabWidget =======================
	public static final int[] TabWidget = {
		16843049, 16843451, 16843452, 16843453
	};
	public static final int TabWidget_divider = 0;
	public static final int TabWidget_tabStripEnabled = 3;
	public static final int TabWidget_tabStripLeft  = 1;
	public static final int TabWidget_tabStripRight = 2;


}
