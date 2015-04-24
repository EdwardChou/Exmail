package com.cuhk.cse.exmail.view;

import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;

public class OnListItemExpandListener implements OnGroupExpandListener {

	protected ExpandableListView listView;
	private int seletedGroupID = -1;

	public int getSeletedGroupID() {
		return seletedGroupID;
	}

	public OnListItemExpandListener(ExpandableListView listView) {
		this.listView = listView;
	}

	@Override
	public void onGroupExpand(int groupPosition) {
		// TODO Auto-generated method stub
		for (int i = 0, count = listView.getExpandableListAdapter()
				.getGroupCount(); i < count; i++) {
			if (groupPosition != i) { // 关闭其他
				if (listView.isGroupExpanded(groupPosition)) {
					listView.collapseGroup(i);
				}
			} else if (groupPosition == listView.getFirstVisiblePosition()) { // 让第一条可见
				// android1.6不支持以下方法
				seletedGroupID = groupPosition;
				listView.smoothScrollToPosition(groupPosition);
			}
		}
	}

}
