package com.ajie.chilli.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * KVpair集合
 *
 * @author niezhenjie
 *
 */
public class KVpairs {
	/** 按id升序排序 */
	public static final int ID_SORT_DESC = 1;
	/** 按id降序排序 */
	public static final int ID_SORT_ASC = 2;

	private List<KVpair> datas;

	private KVpairs() {
		datas = new ArrayList<KVpair>();
	}

	private KVpairs(int size) {
		datas = new ArrayList<KVpair>(size);
	}

	public List<KVpair> getDatas() {
		return datas;
	}

	public void addItem(KVpair item) {
		datas.add(item);
	}

	public void removeItem(int idx) {
		datas.remove(idx);
	}

	public void removeItem(KVpair item) {
		datas.remove(item);
	}

	public List<Integer> getIds() {
		List<Integer> list = new ArrayList<Integer>(datas.size());
		for (KVpair p : datas) {
			list.add(p.getId());
		}
		return list;
	}

	/**
	 * 根据id排序
	 * 
	 * @param flag
	 *            见KVpairs.ID_SORT_XXX
	 */
	public void sortById(int flag) {
		final boolean desc = flag == ID_SORT_DESC;
		Comparator<KVpair> comparator = new Comparator<KVpair>() {
			@Override
			public int compare(KVpair o1, KVpair o2) {
				if (o1 == null) {
					return desc ? -1 : 1;
				}
				if (o2 == null) {
					return desc ? 1 : -1;
				}
				int ret = o1.getId() - o2.getId();
				if (ret > 0) {
					return desc ? 1 : -1;
				} else {
					return desc ? -1 : 1;
				}

			}
		};
		Collections.sort(datas, comparator);
	}

	public static KVpairs valueOf(KVpair... params) {
		if (null == params || params.length == 0) {
			return new KVpairs(0);
		}
		KVpairs pairs = new KVpairs(params.length);
		for (KVpair p : params) {
			pairs.addItem(p);
		}
		return pairs;
	}

	public boolean contain(KVpair item) {
		return datas.contains(item);
	}

	/**
	 * 是否含有id项
	 * 
	 * @param id
	 * @return
	 */
	public boolean containId(int id) {
		for (KVpair p : datas) {
			if (p.getId() == id) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 删除重复id项，注，如果KVpair只有name和value会全部删除只留一项
	 */
	public void deleteRepeatId() {
		List<KVpair> list = new ArrayList<KVpair>();
		boolean b = false;
		for (int i = 0; i < datas.size(); i++) {
			KVpair item = datas.get(i);
			for (int j = i + 1; j < datas.size(); j++) {
				if (item.getId() == datas.get(j).getId()) {
					b = true;
					break;
				}
			}
			if (!b) {
				list.add(item);
			}
		}
		datas = list;
	}

	/**
	 * 返回state&id==id 的id
	 * 
	 * @param state
	 * @return
	 */
	public List<Integer> getStates(int state) {
		List<Integer> ids = new ArrayList<Integer>();
		for (KVpair p : datas) {
			int id = p.getId();
			if (id == (id & state)) {
				ids.add(id);
			}
		}
		return ids;
	}

	/**
	 * 返回state&id == id的项
	 * 
	 * @param state
	 * @return
	 */
	public List<KVpair> getStateItems(int state) {
		List<KVpair> items = new ArrayList<KVpair>();
		for (KVpair p : datas) {
			int id = p.getId();
			if (id == (id & state)) {
				items.add(p);
			}

		}
		return items;
	}
}
