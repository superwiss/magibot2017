import java.util.List;
import java.util.Observable;
import java.util.Observer;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;

public abstract class StrategyBase implements Observer {

	protected StrategyManager strategyManager;
	protected CommandUtil commandUtil;

	private StrategyManager.Strategy myStrategy;

	public StrategyBase(StrategyManager.Strategy strategy) {
		myStrategy = strategy;
	}

	// 서플라이가 다 꽉찼을때 새 서플라이를 지으면 지연이 많이 일어나므로, supplyMargin (게임에서의 서플라이 마진 값의 2배)만큼 부족해지면 새 서플라이를 짓도록 한다
	// 이렇게 값을 정해놓으면, 게임 초반부에는 서플라이를 너무 일찍 짓고, 게임 후반부에는 서플라이를 너무 늦게 짓게 된다
	private int supplyMargin = 12;
	// 
	private int thresholdMineralsForTraning = 200;

	public abstract void init();

	public abstract void buildOrder();

	public void workerTraining() {

		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (strategyManager.initialBuildOrderFinished() == false) {
			return;
		}

		if (MyBotModule.Broodwar.self().minerals() >= 50) {
			// workerCount = 현재 일꾼 수 + 생산중인 일꾼 수
			int workerCount = MyBotModule.Broodwar.self().allUnitCount(InformationManager.Instance().getWorkerType());

			if (MyBotModule.Broodwar.self().getRace() == Race.Zerg) {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType() == UnitType.Zerg_Egg) {
						// Zerg_Egg 에게 morph 명령을 내리면 isMorphing = true,
						// isBeingConstructed = true, isConstructing = true 가 된다
						// Zerg_Egg 가 다른 유닛으로 바뀌면서 새로 만들어진 유닛은 잠시
						// isBeingConstructed = true, isConstructing = true 가
						// 되었다가,
						if (unit.isMorphing() && unit.getBuildType() == UnitType.Zerg_Drone) {
							workerCount++;
						}
					}
				}
			} else {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType().isResourceDepot()) {
						if (unit.isTraining()) {
							workerCount += unit.getTrainingQueue().size();
						}
					}
				}
			}

			if (workerCount < 30) {
				for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
					if (unit.getType().isResourceDepot()) {
						if (unit.isTraining() == false || unit.getLarva().size() > 0) {
							// 빌드큐에 일꾼 생산이 1개는 있도록 한다
							if (BuildManager.Instance().buildQueue.getItemCount(InformationManager.Instance().getWorkerType(), null) == 0) {
								// std.cout << "worker enqueue" << std.endl;
								BuildManager.Instance().buildQueue.queueAsLowestPriority(new MetaType(InformationManager.Instance().getWorkerType()), false);
							}
						}
					}
				}
			}
		}
	}

	public void supplyManagement() {
		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (strategyManager.initialBuildOrderFinished() == false) {
			return;
		}

		// 1초에 한번만 실행
		if (MyBotModule.Broodwar.getFrameCount() % 24 != 0) {
			return;
		}

		// 게임에서는 서플라이 값이 200까지 있지만, BWAPI 에서는 서플라이 값이 400까지 있다
		// 저글링 1마리가 게임에서는 서플라이를 0.5 차지하지만, BWAPI 에서는 서플라이를 1 차지한다
		if (MyBotModule.Broodwar.self().supplyTotal() <= 400) {

			// currentSupplyShortage 를 계산한다
			int currentSupplyShortage = MyBotModule.Broodwar.self().supplyUsed() + supplyMargin - MyBotModule.Broodwar.self().supplyTotal();

			if (currentSupplyShortage > 0) {

				// 생산/건설 중인 Supply를 센다
				int onBuildingSupplyCount = 0;

				// 저그 종족인 경우, 생산중인 Zerg_Overlord (Zerg_Egg) 를 센다. Hatchery 등 건물은 세지 않는다
				if (MyBotModule.Broodwar.self().getRace() == Race.Zerg) {
					for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
						if (unit.getType() == UnitType.Zerg_Egg && unit.getBuildType() == UnitType.Zerg_Overlord) {
							onBuildingSupplyCount += UnitType.Zerg_Overlord.supplyProvided();
						}
						// 갓태어난 Overlord 는 아직 SupplyTotal 에 반영안되어서, 추가 카운트를 해줘야함
						if (unit.getType() == UnitType.Zerg_Overlord && unit.isConstructing()) {
							onBuildingSupplyCount += UnitType.Zerg_Overlord.supplyProvided();
						}
					}
				}
				// 저그 종족이 아닌 경우, 건설중인 Protoss_Pylon, Terran_Supply_Depot 를 센다. Nexus, Command Center 등 건물은 세지 않는다
				else {
					onBuildingSupplyCount += ConstructionManager.Instance().getConstructionQueueItemCount(InformationManager.Instance().getBasicSupplyProviderUnitType(), null) * InformationManager.Instance().getBasicSupplyProviderUnitType().supplyProvided();
				}

				//System.out.println("currentSupplyShortage : " + currentSupplyShortage + " onBuildingSupplyCount : " + onBuildingSupplyCount);

				if (currentSupplyShortage > onBuildingSupplyCount) {

					// BuildQueue 최상단에 SupplyProvider 가 있지 않으면 enqueue 한다
					boolean isToEnqueue = true;
					if (!BuildManager.Instance().buildQueue.isEmpty()) {
						BuildOrderItem currentItem = BuildManager.Instance().buildQueue.getHighestPriorityItem();
						if (currentItem.metaType.isUnit() && currentItem.metaType.getUnitType() == InformationManager.Instance().getBasicSupplyProviderUnitType()) {
							isToEnqueue = false;
						}
					}
					if (isToEnqueue) {
						System.out.println("enqueue supply provider " + InformationManager.Instance().getBasicSupplyProviderUnitType());
						BuildManager.Instance().buildQueue.queueAsHighestPriority(new MetaType(InformationManager.Instance().getBasicSupplyProviderUnitType()), true);
					}
				}
			}
		}
	}

	public void combatUnitTraining() {
		// InitialBuildOrder 진행중에는 아무것도 하지 않습니다
		if (strategyManager.initialBuildOrderFinished() == false) {
			return;
		}

		// 기본 병력 추가 훈련
		if (MyBotModule.Broodwar.self().minerals() >= thresholdMineralsForTraning && MyBotModule.Broodwar.self().supplyUsed() < 390) {
			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType() == InformationManager.Instance().getBasicCombatBuildingType()) {
					if (unit.isTraining() == false || unit.getLarva().size() > 0) {
						if (BuildManager.Instance().buildQueue.getItemCount(InformationManager.Instance().getBasicCombatUnitType(), null) == 0) {
							BuildManager.Instance().buildQueue.queueAsLowestPriority(InformationManager.Instance().getBasicCombatUnitType(), BuildOrderItem.SeedPositionStrategy.MainBaseLocation, true);
						}
					}
				}
			}
		}
	}

	public void executeCombat() {

		// 공격 모드가 아닐 때에는 전투유닛들을 아군 진영 길목에 집결시켜서 방어
		if (strategyManager.fullScaleAttackStarted() == false) {
			Position standbyPosition = null;

			// 본진 앞 길목에서 대기한다.
			standbyPosition = BWTA.getNearestChokepoint(InformationManager.Instance().getMainBaseLocation(InformationManager.Instance().selfPlayer).getTilePosition()).getCenter();

			for (Unit unit : MyBotModule.Broodwar.self().getUnits()) {
				if (unit.getType() == InformationManager.Instance().getBasicCombatUnitType() && unit.isIdle()) {
					commandUtil.attackMove(unit, standbyPosition);
				}
			}

			// 적군 위치가 파악되었으면 총공격 모드로 전환
			if (MyBotModule.Broodwar.self().completedUnitCount(InformationManager.Instance().getBasicCombatUnitType()) > 2) {
				if (InformationManager.Instance().enemyPlayer != null && InformationManager.Instance().enemyRace != Race.Unknown && InformationManager.Instance().getOccupiedBaseLocations(InformationManager.Instance().enemyPlayer).size() > 0) {
					strategyManager.fullScaleAttackStarted(true);
				}
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

	public void terminator() {
		if (strategyManager.fullScaleAttackStarted()) {
			if (MyBotModule.Broodwar.self().allUnitCount() > MyBotModule.Broodwar.enemy().allUnitCount() * 3) {
				for (Unit enemyUnit : MyBotModule.Broodwar.enemy().getUnits()) {
					if (enemyUnit.getType().isBuilding()) {
						for (Unit myUnit : MyBotModule.Broodwar.self().getUnits()) {
							if (myUnit.canAttack()) {
								if (myUnit.isIdle()) {
									commandUtil.attackMove(myUnit, enemyUnit.getPosition());
								}
							}
						}
					}
				}

				// 놀고 있는 오버로드가 있고, 아직 확장 지역을 정찰 안했으면, 정찰을 한다.
				if (false == strategyManager.foundFirstEnemyExpansion()) {
					for (Unit unit : InformationManager.Instance().selfPlayer.getUnits()) {
						if (unit.getType().equals(UnitType.Zerg_Overlord)) {
							if (unit.isIdle()) {
								BaseLocation enemyFirstExpansionLocation = InformationManager.Instance().getFirstExpansionLocation(InformationManager.Instance().enemyPlayer);
								if (null != enemyFirstExpansionLocation) {
									unit.move(enemyFirstExpansionLocation.getPosition());
									strategyManager.foundFirstEnemyExpansion(true);
								}
							}
						}
					}
				}
			}
		}

	}

	@Override
	public void update(Observable o, Object obj) {
		if (null != obj) {
			strategyManager = StrategyManager.Instance();
			if (strategyManager.getStrategy().equals(myStrategy)) {
				commandUtil = strategyManager.getCommandUtil();
				init();
				buildOrder();
			}
		} else {
			if (strategyManager.getStrategy().equals(myStrategy)) {
				workerTraining();
				supplyManagement();
				combatUnitTraining();
				executeCombat();
				terminator();
			}
		}
	}

	public int getSupplyMargin() {
		return this.supplyMargin;
	}

	public void setSupplyMargin(int supplyMargin) {
		this.supplyMargin = supplyMargin;
	}

	public int getThresholdMineralsForTraning() {
		return this.supplyMargin;
	}

	public void setThresholdMineralsForTraning(int thresholdMineralsForTraning) {
		this.thresholdMineralsForTraning = thresholdMineralsForTraning;
	}

}
