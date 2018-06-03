package com.magiai;
import java.util.List;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;

public class Strategy9Drone extends StrategyBase {

	public Strategy9Drone(StrategyManager.Strategy strategy) {
		super(strategy);
	}

	@Override
	public void init() {
		setSupplyMargin(4);
		setThresholdMineralsForTraning(50);
	}

	@Override
	public void buildOrder() {
		// 드론을 9마리 까지 생성한다.
		BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(), BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(), BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(), BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(), BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(), BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		// 스포닝 풀 짓기
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Spawning_Pool, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		// 드론 1기 보충
		BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(), BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		// 오버로드 보충
		BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(), BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		// 드론 1기 보충
		BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(), BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		// 6저글링
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		// 해처리 추가
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hatchery, BuildOrderItem.SeedPositionStrategy.FirstExpansionLocation);
		// 4저글링
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		// 오버로드 보충
		BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicSupplyProviderUnitType(), BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		// 해처리 추가
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Hatchery, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		// 12 저글링
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		BuildManager.Instance().buildQueue.queueAsLowestPriority(UnitType.Zerg_Zergling, BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
		// 드론 1기 보충
		BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getWorkerType(), BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
	}

	@Override
	public void workerTraining() {
		// 더 이상 드론을 생성하지 않는다.
		return;
	}

	@Override
	public void supplyManagement() {
		// 기본 설정을 그대로 가져간다.
		super.supplyManagement();
	}

	@Override
	public void combatUnitTraining() {
		// 기본 설정을 그대로 가져간다.
		super.combatUnitTraining();
	}

	@Override
	public void executeCombat() {

		// 공격 모드가 아닐 때에는 전투유닛들을 아군 진영 길목에 집결시켜서 방어
		if (strategyManager.fullScaleAttackStarted() == false) {
			Position standbyPosition = null;

			// 4Drone일 경우, 집결 위치는 맵 중앙
			standbyPosition = new Position(MyBotModule.Broodwar.mapWidth() / 2 * 32, MyBotModule.Broodwar.mapHeight() / 2 * 32);

			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType() == InformationManager.Instance().getBasicCombatUnitType() && unit.isIdle()) {
					commandUtil.attackMove(unit, standbyPosition);
				}
			}

			// 전투 유닛이 2개 이상 생산되었고, 적군 위치가 파악되었으면 총공격 모드로 전환
			if (MyBotModule.Broodwar.self().completedUnitCount(InformationManager.Instance().getBasicCombatUnitType()) > 2) {
				// 4드론 전략일 경우 적군의 위치가 발견되지 않았으면 저글링으로 정찰을 보낸다.
				// 저글링 두 마리를 남은 곳으로 정찰 보낸다.
				if (0 == ScoutManager.Instance().getExtraScoutUnitsSize()) {
					Unit scoutUnit1 = null;
					Unit scoutUnit2 = null;
					for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
						if (unit.getType() == InformationManager.Instance().getBasicCombatUnitType()) {
							if (null == scoutUnit1) {
								scoutUnit1 = unit;
							} else if (null == scoutUnit2) {
								scoutUnit2 = unit;
								break;
							}
						}
					}
					scoutUnit1.move(InformationManager.Instance().getFirstChokePoint(MyBotModule.Broodwar.self()).getCenter());
					scoutUnit2.move(InformationManager.Instance().getFirstChokePoint(MyBotModule.Broodwar.self()).getCenter());
					List<BaseLocation> remainLocation = ScoutManager.Instance().getRemainEnemyStartLocatons();
					if (remainLocation.size() > 0) {
						ScoutManager.Instance().registerExtraScoutUnit(scoutUnit1, remainLocation.get(0).getPosition());
					}
					if (remainLocation.size() > 1) {
						ScoutManager.Instance().registerExtraScoutUnit(scoutUnit2, remainLocation.get(1).getPosition());
					}
				}
				// 적이 발견되면 저글링 정찰을 중단하고 공격한다.
				if (InformationManager.Instance().enemyPlayer != null && InformationManager.Instance().enemyRace != Race.Unknown && InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer).size() > 0) {
					strategyManager.fullScaleAttackStarted(true);
				}
				// 적을 찾지 못했지만, 남은 장소가 한 곳이라면, 그곳으로 공격을 들어간다.
				if (1 == ScoutManager.Instance().getRemainEnemyStartLocatons().size()) {
					strategyManager.fullScaleAttackStarted(true);
				}
			} else if (InformationManager.Instance().enemyPlayer != null && InformationManager.Instance().enemyRace != Race.Unknown && InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer).size() > 0) {
				strategyManager.fullScaleAttackStarted(true);
			}
		}
		// 공격 모드가 되면, 모든 전투유닛들을 적군 Main BaseLocation 로 공격 가도록 합니다
		else {
			List<BaseLocation> remainLocation = ScoutManager.Instance().getRemainEnemyStartLocatons();

			ScoutManager.Instance().unregusterAllExtraScoutUnits();

			if (InformationManager.Instance().enemyPlayer != null && InformationManager.Instance().enemyRace != Race.Unknown && InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer).size() > 0 || 1 == remainLocation.size()) {
				// 공격 대상 지역 결정
				BaseLocation targetBaseLocation = null;
				double closestDistance = 100000000;

				for (BaseLocation baseLocation : InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer)) {
					double distance = BWTA.getGroundDistance(InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer).getTilePosition(), baseLocation.getTilePosition());

					if (distance < closestDistance) {
						closestDistance = distance;
						targetBaseLocation = baseLocation;
					}
				}

				if (null == targetBaseLocation) {
					targetBaseLocation = remainLocation.get(0);
				}

				if (targetBaseLocation != null) {
					for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
						// 건물은 제외
						if (unit.getType().isBuilding()) {
							continue;
						}
						// 모든 일꾼은 제외
						if (unit.getType().isWorker()) {
							continue;
						}

						// canAttack 유닛은 attackMove Command 로 공격을 보냅니다
						if (unit.canAttack()) {

							if (unit.isIdle()) {
								commandUtil.attackMove(unit, targetBaseLocation.getPosition());
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void terminator() {
		// 기본 설정을 그대로 가져간다.
		super.terminator();
	}
}
