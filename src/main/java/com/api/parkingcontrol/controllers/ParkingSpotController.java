package com.api.parkingcontrol.controllers;

import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.parkingcontrol.dtos.ParkingSpotDTO;
import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.services.ParkingSpotService;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {

	final ParkingSpotService parkingSpotService;

	public ParkingSpotController(ParkingSpotService parkingSpotService) {
		this.parkingSpotService = parkingSpotService;
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping
	public ResponseEntity<Object> saveParkingControl(@RequestBody @Valid ParkingSpotDTO parkingSpotDTO) {

		if (parkingSpotService.existsByLicensePlateCar(parkingSpotDTO.getLicensePlateCar())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: License Plate Car is already in use!");
		}

		if (parkingSpotService.existsByParkingSpotNumber(parkingSpotDTO.getParkingSpotNumber())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking Spot is already in use!");
		}

		if (parkingSpotService.existsByApartmentAndBlock(parkingSpotDTO.getApartment(), parkingSpotDTO.getBlock())) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body("Conflict: Parking Spot is already registered for this apartment/block!");
		}

		var parkingSpotModel = new ParkingSpotModel();
		BeanUtils.copyProperties(parkingSpotDTO, parkingSpotModel);
		parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
		return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel));
	}

	@PreAuthorize("hasRole('ROLE_ADMIN', 'ROLE_USER')")
	@GetMapping
	public ResponseEntity<Page<ParkingSpotModel>> getAllParkingSpots(
			@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll(pageable));
	}

	@PreAuthorize("hasRole('ROLE_ADMIN', 'ROLE_USER')")
	@GetMapping("/{id}")
	public ResponseEntity<Object> getOneParkingSpot(@PathVariable(value = "id") Long id) {
		
		ParkingSpotModel parkingSpotModel = parkingSpotService.findById(id);
		
		if (parkingSpotModel == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found.");
		}
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpotModel);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Object> deleteParkingSpot(@PathVariable(value = "id") Long id) {

		ParkingSpotModel parkingSpotModel = parkingSpotService.findById(id);

		if (parkingSpotModel == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found");
		}
		parkingSpotService.delete(parkingSpotModel);
		return ResponseEntity.status(HttpStatus.OK).body("Parking Spot deleted successfuly.");
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PutMapping("/{id}")
	public ResponseEntity<Object> updateParkingSpot(@PathVariable(value = "id") Long id,
			@RequestBody @Valid ParkingSpotDTO parkingSpotDTO) {
		
		try {
			ParkingSpotModel parkingSpotModelPesquisado = parkingSpotService.findById(id);
			
			if (parkingSpotModelPesquisado == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found");
			}
			
			// 2a maneira de atualizar um objeto que possui muitos dados:
			var parkingSpotModel = new ParkingSpotModel();
			BeanUtils.copyProperties(parkingSpotDTO, parkingSpotModel);
			parkingSpotModel.setId(parkingSpotModelPesquisado.getId());
			parkingSpotModel.setRegistrationDate(parkingSpotModelPesquisado.getRegistrationDate());
			
			return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpotModel));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.notFound().build();
		}

		/*
		 * 1a maneira de atualizar um objeto: var parkingSpotModel =
		 * parkingSpotModelOptional.get();
		 * parkingSpotModel.setParkingSpotNumber(parkingSpotDTO.getParkingSpotNumber()t)
		 * ; parkingSpotModel.setLicensePlateCar(parkingSpotDTO.getLicensePlateCar());
		 * parkingSpotModel.setModelCar(parkingSpotDTO.getModelCar());
		 * parkingSpotModel.setBrandCar(parkingSpotDTO.getBrandCar());
		 * parkingSpotModel.setColorCar(parkingSpotDTO.getColorCar());
		 * parkingSpotModel.setResponsibleName(parkingSpotDTO.getResponsibleName());
		 * parkingSpotModel.setApartment(parkingSpotDTO.getApartment());
		 * parkingSpotModel.setBlock(parkingSpotDTO.getBlock());
		 */

	}
}
