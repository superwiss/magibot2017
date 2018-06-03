package com.magiai;

import com.magiai.spec.UnitManager;
import com.magiai.utils.StringUtil;

import bwapi.Game;
import bwapi.Unit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Model {
	private static Game game = MyBotModule.Broodwar;
	private UnitManager myUnitManager = new UnitManager();
	private UnitManager enemyUnitManager = new UnitManager();

	public void addUnit(Unit unit) {
		boolean isBuilding = unit.getType().isBuilding();
		if (game.self().isEnemy(unit.getPlayer())) {
			// 상대방 유닛일 경우
			if (!isBuilding) {
				enemyUnitManager.add(unit);
			}
		} else {
			// 내 유닛일 경우
			if (!isBuilding) {
				myUnitManager.add(unit);
			}
		}
	}

	public void removeUnit(Unit unit) {
		boolean isBuilding = unit.getType().isBuilding();
		if (game.self().isEnemy(unit.getPlayer())) {
			// 상대방 유닛일 경우
			if (!isBuilding) {
				log.info(LogFormatter.info("\tEnemy Unit has terminated", StringUtil.printUnitInfo(unit)));
				enemyUnitManager.remove(unit);
			}
		} else {
			// 내 유닛일 경우
			if (!isBuilding) {
				log.info(LogFormatter.info("\tMy Unit has terminated", StringUtil.printUnitInfo(unit)));
				myUnitManager.remove(unit);
			}
		}
	}

	@Override
	public String toString() {
		String result = "";

		result = String.format("MyUnit: %s, EnemyUnit: %s", myUnitManager.toString(), enemyUnitManager.toString());

		return result;
	}
}