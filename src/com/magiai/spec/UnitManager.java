package com.magiai.spec;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.magiai.constant.UnitStatus;
import com.magiai.utils.StringUtil;

import bwapi.Unit;
import lombok.Data;

@Data
public class UnitManager {
	private List<Unit> unitList = new LinkedList<>();
	private Map<Integer, Unit> unitIdMap = new HashMap<>();
	private Map<Integer, UnitStatus> unitStatusMap = new HashMap<>();
	private Map<Unit, Unit> enemyUnitTargetmap = new HashMap<>();

	public boolean add(Unit unit) {
		unitIdMap.put(unit.getID(), unit);
		unitStatusMap.put(unit.getID(), UnitStatus.IDLE);
		return unitList.add(unit);
	}

	public boolean remove(Unit unit) {
		unitIdMap.remove(unit.getID());
		unitStatusMap.remove(unit.getID());
		return unitList.remove(unit);
	}

	public Unit getUnitById(int id) {
		return unitIdMap.get(id);
	}

	public UnitStatus getUnitStatus(Unit unit) {
		return unitStatusMap.get(unit.getID());
	}

	public void setUnitStatus(Unit unit, UnitStatus unitStatus) {
		unitStatusMap.put(unit.getID(), unitStatus);
	}

	public Unit getEnemy(Unit myUnit) {
		return enemyUnitTargetmap.get(myUnit);
	}

	public Unit setEnemy(Unit myUnit, Unit enemyUnit) {
		return enemyUnitTargetmap.put(myUnit, enemyUnit);
	}

	@Override
	public String toString() {
		String result = "";

		for (Unit unit : unitList) {
			result += "(" + StringUtil.printUnit(unit) + ") ";
		}

		return result;
	}
}