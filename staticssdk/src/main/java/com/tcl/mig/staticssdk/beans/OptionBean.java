package com.tcl.mig.staticssdk.beans;

/**
 * Option选项的bean，为upload提供选项
 * 
 * @author luozhiping
 *
 */
public class OptionBean {
	public static final int OPTION_INDEX_IMMEDIATELY_CARE_SWITCH = 0;
	public static final int OPTION_INDEX_POSITION = 1;
	public static final int OPTION_INDEX_ABTEST = 2;
	public static final int OPTION_INDEX_IMMEDIATELY_ANYWAY = 3;
	private int mOptionID = -1;
	private Object mOptionContent = null;

	public OptionBean(int optionID, Object optionContent) {
		this.mOptionID = optionID;
		this.mOptionContent = optionContent;
		checkOptionContentType();
	}

	public int getOptionID() {
		return mOptionID;
	}

	public Object getOptionContent() {
		return mOptionContent;
	}

	private void checkOptionContentType() {
		if ((mOptionID == OPTION_INDEX_IMMEDIATELY_ANYWAY || mOptionID == OPTION_INDEX_IMMEDIATELY_CARE_SWITCH)
				&& !(mOptionContent instanceof Boolean)) {
			throw new IllegalArgumentException("Immediately argument must be 'true' or 'false'");
		} else if ((mOptionID == OPTION_INDEX_ABTEST || mOptionID == OPTION_INDEX_POSITION)
				&& !(mOptionContent instanceof String)) {
			throw new IllegalArgumentException("Position or ABTest argument type must be String");
		}
	}
}
