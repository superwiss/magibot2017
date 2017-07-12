import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import bwapi.TilePosition;

public class MapInfo extends Observable {

	private static MapInfo instance = new MapInfo();

	public static enum MapType {
		Unknown, // 기타 맵
		LostTemple, // 로템
		FightingSpirit, // 투혼
		Hunters // 헌터
	};

	// 투혼: 1시(117, 7), 5시(117,117), 7시(7, 116), 11시(7, 6)
	//private TilePosition[] mapFighting = { new TilePosition(7, 117), new TilePosition(117, 117), new TilePosition(7, 116), new TilePosition(6, 7) };
	private TilePosition[] mapFighting = { new TilePosition(117, 7), new TilePosition(117, 117), new TilePosition(7, 116), new TilePosition(7, 6) };

	private MapType mapType = MapType.Unknown;
	private TilePosition myTilePosition;
	private List<TilePosition> order = new ArrayList<>(8);

	/// static singleton 객체를 리턴합니다
	public static MapInfo Instance() {
		return instance;
	}

	public MapInfo() {
		// 현재 지도를 설정한다.
		String mapFileName = MyBotModule.Broodwar.mapFileName();
		if (mapFileName.contains("Temple")) {
			//mapType = MapType.LostTemple;
		} else if (mapFileName.contains("Fighting")) {
			mapType = MapType.FightingSpirit;
		} else if (mapFileName.contains("Hunters")) {
			//mapType = MapType.Hunters;
		}
		System.out.println(String.format("MapType: %s", mapType));

		// 내 시작 위치를 설정한다.
		myTilePosition = InformationManager.Instance().getMainBaseLocation(MyBotModule.Broodwar.self()).getTilePosition();
		System.out.println(String.format("MyTilePosition: %s", myTilePosition));

		// 정찰 순서를 결정한다.
		switch (mapType) {
		case FightingSpirit:
			if (myTilePosition.equals(mapFighting[0])) {
				// 내 위치가 1시라면, 정찰은 11시, 5시, 7시 순서
				order.add(mapFighting[3]);
				order.add(mapFighting[1]);
				order.add(mapFighting[2]);
			} else if (myTilePosition.equals(mapFighting[1])) {
				// 내 위치가 5시라면, 정찰은 7시, 1시, 11시 순서
				order.add(mapFighting[2]);
				order.add(mapFighting[0]);
				order.add(mapFighting[3]);
			} else if (myTilePosition.equals(mapFighting[2])) {
				// 내 위치가 7시라면, 정찰은 5시, 11시, 1시 순서
				order.add(mapFighting[1]);
				order.add(mapFighting[3]);
				order.add(mapFighting[0]);
			} else if (myTilePosition.equals(mapFighting[3])) {
				// 내 위치가 11시라면, 정찰은 1시, 7시, 5시 순서
				order.add(mapFighting[0]);
				order.add(mapFighting[2]);
				order.add(mapFighting[1]);
			}
		default:
			break;
		}
	}

	// order 번째로 정찰해야 할 TilePosition을 리턴
	public TilePosition getSearchingOrder(int searchOrder) {
		TilePosition result = null;

		if (order.size() >= searchOrder) {
			result = order.get(searchOrder);
		}

		System.out.println(String.format("Order: %d, Location: (%s)", searchOrder, result));

		return result;
	}

	// 정찰 순서를 알 수 있는 맵이라면 true를 리턴, 정찰 순서를 모르는 맵이라면 false를 리턴
	public boolean isKnownMap() {
		if (mapType.equals(MapType.Unknown)) {
			return false;
		}
		return true;
	}

}