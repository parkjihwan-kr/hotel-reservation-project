package com.nbc.hotel.model;



import com.nbc.hotel.exception.ReservationNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hotel {
	private List<Room> rooms = new ArrayList<>();				// 현재 방목록
	private List<Reservation> reservations = new ArrayList<>();	// 현재 예약목록
	private Double money;										// 총자산

	Reservation reservation = new Reservation();

	Customer customer = new Customer();
	
	private final String BLACKLISTPHONENUMBER_PATTERN = "010-2222-\\d+";
    private Pattern blacklistPattern = Pattern.compile(BLACKLISTPHONENUMBER_PATTERN);
    // 010-2222에 해당하는 모든 숫자조합은 블랙리스트패턴에 걸려서 호텔 서비스를 활용 불가합니다.
    
	public Hotel() {
		rooms.add(new Room(1.0,90.0));
		rooms.add(new Room(1.0,110.0));
		rooms.add(new Room(1.0,130.0));
		rooms.add(new Room(1.0,190.0));
		
		rooms.add(new Room(2.0,140.0));
		rooms.add(new Room(2.0,160.0));
		rooms.add(new Room(2.0,200.0));
		rooms.add(new Room(2.0,220.0));
	}

	Boolean cancelReservations;

	public void cancelReservation(Reservation targetReservation) throws ReservationNotFoundException {
		if (!reservations.remove(targetReservation)) {
			throw new ReservationNotFoundException();
		}
	}

	/**
	 * 예약이 존재하면 예약을 돌려주고, 없다면 ReservationNotFoundException 던진다
	 *
	 * @param reservationId
	 * @return
	 */
	public Reservation findReservationByUUID(UUID reservationId) {
		return reservations
				.stream()
				.filter(reservation -> reservation.getReservationId().compareTo(reservationId) == 0)
				.findFirst()
				.orElseThrow(ReservationNotFoundException::new);
	}


	public List<Reservation> findAllReservations() {
		return reservations;
	}

	/**
	 * @param uuids
	 * @return
	 */
	public List<Reservation> findReservationsByUUIDs(List<UUID> uuids) {
		return reservations
				.stream()
				.filter(reservation -> uuids.contains(reservation.getUUID()))
				.toList();
	}
	
	
	public List<Room> getRooms(){
		return this.rooms;
	}

	public Customer getCustomer() {
		return customer;
	}
	
	public List<Reservation> getReservations() {
		return reservations;
	}
	
	// 예약 처리 프로세스
	public void checkCustomerName(String customerName, int selectRoomNumber) throws Exception {
		try {
	        if (!customerName.matches("^[a-zA-Z가-힣\\s]*$")) {
	            throw new IllegalArgumentException("예약자 이름은 특수 문자를 포함해서는 안 됩니다.");
	        }
	        
	        UUID uuid = UUID.randomUUID();
	        reservation.setUUID(uuid);
	        customer.addReservationId(uuid);
	        // 예약 번호 설정
	        customer.setName(customerName);
	        // 예약자 이름 설정
	        reservation.setRoom(getRooms().get(selectRoomNumber));
	        // 예약 리스트 room 설정
	        reservation.setCustomerName(customerName);
	        // 예약자 이름 설정
	    } catch (IllegalArgumentException e) {
	        System.out.println("예외 발생: " + e.getMessage());
	        System.out.println("예약 페이지로 다시 돌아갑니다.");
            System.out.println("============================");
            throw new Exception();
	    }
	}
	
	public boolean checkBlackListPhoneNumber(String customerPhoneNumber) {
	    boolean isBlackList = false;

	    try {
	    	String regex = "^010-[0-9]{4}-[0-9]{4}$";
	    	// 숫자와 하이픈만을 허용하는 정규표현식
	        Matcher matcher = Pattern.compile(regex).matcher(customerPhoneNumber);

	        if (!matcher.matches()) {
	            throw new IllegalArgumentException("올바른 전화번호 형식이 아닙니다. 다시 시도해주세요!");
	        } else if (blacklistPattern.matcher(customerPhoneNumber).matches()) {
	            throw new IllegalArgumentException("해당 사용자는 블랙리스트인 사용자입니다.");
	        } else {
	            customer.setPhoneNumber(customerPhoneNumber);
	            reservation.setCustomerPhoneNumber(customerPhoneNumber);
	            isBlackList = false;
	        }
	    } catch (IllegalArgumentException e) {
	        System.out.println("예외 발생: " + e.getMessage());
	        // 예외 처리 로직 추가
	        isBlackList = true;
	    }
	    return isBlackList;
	}

	public boolean checkMoney(double customerMoney, double selectRoomPrice, String selectDate) {
		customer.setMoney(customerMoney);
		boolean myAnswer = reservation.hasMoney(customer, selectRoomPrice);
		
		if(myAnswer) {
			// 고객의 돈이 호텔 룸 예약 가격보다 더 많을때
    		reservation.finalReservation(selectDate, reservations);
		}
		return true;
	}
}
