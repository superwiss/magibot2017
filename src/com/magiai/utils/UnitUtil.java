package com.magiai.utils;

import java.util.LinkedList;
import java.util.List;

import com.magiai.LogFormatter;
import com.magiai.spec.MarineSpec;
import com.magiai.spec.UnitManager;
import com.magiai.vo.UnitSpec;

import bwapi.Unit;
import bwapi.UnitType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UnitUtil {

	private static UnitSpec marineData = new MarineSpec();

	// 유닛의 타입을 판별해서 스펙을 리턴한다.
	public static UnitSpec getUnitSpec(Unit unit) {
		UnitType unitType = unit.getType();

		if (UnitType.Terran_Marine == unitType) {
			return marineData;
		}

		log.warn(LogFormatter.warn("Can not found CombatData because of undefined unit type: {}"), unit.getType());

		return null;
	}

	// 파라메터로 전달 받은 내 유닛이 공격해야 할 가장 적당한 적 유닛을 선택한다.
	// 적당한 유닛이 없으면 null을 리턴한다.
	public static Unit selectEnemyTargetUnit(Unit myUnit, UnitManager enemyUnitManager) {
		List<Unit> combatDistanceList = new LinkedList<>();
		List<Unit> attackDistanceList = new LinkedList<>();
		UnitSpec unitSpec = UnitUtil.getUnitSpec(myUnit);

		// 전투 반경 내의 유닛이 대상이다.
		for (Unit enemyUnit : enemyUnitManager.getUnitList()) {
			int distance = myUnit.getDistance(enemyUnit);
			if (distance <= unitSpec.getCombatDistance()) {
				combatDistanceList.add(enemyUnit);
			}
			if (distance <= unitSpec.getWeaponMaxRange()) {
				attackDistanceList.add(enemyUnit);
			}
		}

		// TODO 예를 들어 내가 벌쳐라면 드라군보다 질럿을 먼저 때리도록 로직을 상세화 한다.

		if (0 < attackDistanceList.size()) {
			return attackDistanceList.get(0);
		}

		if (0 < combatDistanceList.size()) {
			return combatDistanceList.get(0);
		}

		return null;
	}

	// 내 유닛과 적 유닛의 각도를 구한다.
	public static double getAnagleFromMyUnitToEnemyUnit(Unit myUnit, Unit enemyUnit) {
		int x1 = myUnit.getPosition().getX();
		int y1 = myUnit.getPosition().getY();

		int x2 = enemyUnit.getPosition().getX();
		int y2 = enemyUnit.getPosition().getY();

		int dx = x2 - x1;
		int dy = y2 - y1;

		double rad = -Math.atan2(dy, dx);

		if (rad < 0) {
			rad = Math.PI * 2 + rad;
		}

		return rad;
	}

	// 적 유닛이 나를 바라보고 있는지 구한다.
	public static boolean isEnemyUnitSeeMyUnit(Unit myUnit, Unit enemyUnit, double rad) {
		// 나와 적의 방향
		double angleFromMe = getAnagleFromMyUnitToEnemyUnit(myUnit, enemyUnit);
		double angleFromEnemy = enemyUnit.getAngle();
		double diff = Math.abs(angleFromMe - angleFromEnemy);
		if (diff < rad) {
			return true;
		}
		return false;
	}

	// 한방에 적을 죽일 수 있는지 판단한다.
	public static boolean canKillSingleShoot(Unit myUnit, Unit enemyUnit) {
		// 무기를 사용할 수 없으면 false
		if (0 != myUnit.getGroundWeaponCooldown()) {
			return false;
		}
		// 사거리 밖이면 false
		if (!myUnit.isInWeaponRange(enemyUnit)) {
			return false;
		}
		if (enemyUnit.getHitPoints() <= myUnit.getType().groundWeapon().damageAmount()) {
			return true;
		}
		return false;
	}
}
