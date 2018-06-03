package com.magiai;

import java.util.Observable;

import com.magiai.constant.UnitStatus;
import com.magiai.spec.UnitManager;
import com.magiai.utils.StringUtil;
import com.magiai.utils.UnitUtil;

import bwapi.Position;
import bwapi.Unit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ControlManager extends Observable {

	private static ControlManager instance = new ControlManager();
	private static int oldEnemyHp = 0;
	private static boolean isAttack = false;

	public static ControlManager Instance() {
		return instance;
	}

	public void run(Model model) {
		UnitManager myUnitManager = model.getMyUnitManager();
		UnitManager enemyUnitManager = model.getEnemyUnitManager();
		for (Unit myUnit : myUnitManager.getUnitList()) {

			log.debug(LogFormatter.debug("\tMy Unit: %s, Status: %s", StringUtil.printUnitDebug(myUnit), myUnitManager.getUnitStatus(myUnit)));
			Unit enemyUnit = myUnitManager.getEnemy(myUnit);

			switch (myUnitManager.getUnitStatus(myUnit)) {
			case IDLE:
				// 공격 대상을 찾는다.
				Unit targetUnit = UnitUtil.selectEnemyTargetUnit(myUnit, enemyUnitManager);
				if (null != targetUnit) {
					log.debug(LogFormatter.debug("\tFound Enemy. %s will atack %s", StringUtil.printUnitDebug(myUnit), StringUtil.printUnitDebug(targetUnit)));
					myUnitManager.setEnemy(myUnit, targetUnit);
					enemyUnit = targetUnit;

					myUnitManager.setUnitStatus(myUnit, UnitStatus.WAIT);
					run(model);
				}
				break;
			// TODO wait 기능 만들기
			case WAIT:
			case ATTACK_ENEMY_UNIT:
			case MOVE_TO_EMEMY_UNIT:
			case RUN_AWAY:
				int enemyHp = enemyUnit.getHitPoints();
				int myCooldown = myUnit.getGroundWeaponCooldown();
				int distance = myUnit.getDistance(enemyUnit);
				int enemyTargetId = -1;
				if (null != enemyUnit.getTarget()) {
					enemyTargetId = enemyUnit.getTarget().getID();
				}

				log.trace("\tdistance: {}, cooldown: {}, isAttack: {}, enemyHp: {}, oldEnemyHp: {}, enemyTarget: {}, looking me: {}", distance, myCooldown, isAttack, enemyHp, oldEnemyHp, enemyTargetId, UnitUtil.isEnemyUnitSeeMyUnit(myUnit, enemyUnit, Math.PI));

				// 공격 한 방으로 바로 죽일수 있으면, 거리가 가깝더라도 도망가지 않고 공격한다. 
				if (!isAttack && UnitUtil.canKillSingleShoot(myUnit, enemyUnit)) {
					myUnit.attack(enemyUnit);
					log.debug(LogFormatter.debug("\tFinal Shoot!!! %s will kill %s", StringUtil.printUnitDebug(myUnit), StringUtil.printUnitDebug(enemyUnit)));
					isAttack = true;
					return;
				}

				if (enemyHp < oldEnemyHp) {
					// 공격을 한 번 성공했으면 멀리 도망간다.
					myUnitManager.setUnitStatus(myUnit, UnitStatus.RUN_AWAY);
					myUnit.move(new Position(0, 0));
					isAttack = false;
				} else if (!isAttack) {
					if (0 == myCooldown) {
						// 거리가 많이 가깝더라도 적이 나를 90 degree 오차 내로 나를 바라보고 있지 않으면 바로 공격한다.
						if (!UnitUtil.isEnemyUnitSeeMyUnit(myUnit, enemyUnit, Math.PI) && distance > 60) {
							attackOrMove(myUnitManager, myUnit, enemyUnit);
						} else if (distance > 100) {
							attackOrMove(myUnitManager, myUnit, enemyUnit);
						}
					}
				}
				oldEnemyHp = enemyHp;
				break;
			}
		}
	}

	private void attackOrMove(UnitManager myUnitManager, Unit myUnit, Unit enemyUnit) {
		if (myUnit.isInWeaponRange(enemyUnit)) {
			myUnitManager.setUnitStatus(myUnit, UnitStatus.ATTACK_ENEMY_UNIT);
			myUnit.attack(enemyUnit);
			isAttack = true;
		} else {
			myUnitManager.setUnitStatus(myUnit, UnitStatus.MOVE_TO_EMEMY_UNIT);
			myUnit.move(enemyUnit.getPosition());
		}
	}
}