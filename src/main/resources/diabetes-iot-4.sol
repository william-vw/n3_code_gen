// Specifies the version of Solidity, using semantic versioning.
// Learn more: https://solidity.readthedocs.io/en/v0.5.10/layout-of-source-files.html#pragma
pragma solidity ^0.7.0;

contract DiabetesIot4 {
	string public message;
	
	constructor(string memory initMessage) {
		message = initMessage;
	}
	
	struct Patient {
		Ethnicity hasEthnicity;
		mapping(PatientDemographics => PatientDemographic) hasDemographic;
		mapping(DiabetesPhysicalExaminations => DiabetesPhysicalExamination) hasPhysicalExamination;
		bool exists;
	}
	
	enum PatientDemographics{ Overweight }
	
	struct PatientDemographic {
		PatientDemographics hasType;
		bool exists;
	}
	
	enum DiabetesPhysicalExaminations{ Bmi }
	
	struct DiabetesPhysicalExamination {
		DiabetesPhysicalExaminations hasType;
		int hasQuantitativeValue;
		bool exists;
	}
	
	enum Ethnicities{ AsianAmerican }
	
	struct Ethnicity {
		Ethnicities hasType;
		bool exists;
	}
	
	
	mapping(address => Patient) patients;
	
	function execute() {
		Patient storage patient = patients[msg.sender];
	
		if (patient.hasPhysicalExamination[DiabetesPhysicalExaminations.Bmi].exists
			&& patient.hasPhysicalExamination[DiabetesPhysicalExaminations.Bmi].hasQuantitativeValue >= 25
			&& patient.hasEthnicity.exists
			&& patient.hasEthnicity.hasType != Ethnicities.AsianAmerican) {
		
			PatientDemographic memory v0 = PatientDemographic({ hasType: PatientDemographics.Overweight, exists: true });
			patient.hasDemographic[v0.hasType] = v0;
		}
		
		if (patient.hasPhysicalExamination[DiabetesPhysicalExaminations.Bmi].exists
			&& patient.hasPhysicalExamination[DiabetesPhysicalExaminations.Bmi].hasQuantitativeValue >= 23
			&& patient.hasEthnicity.hasType == Ethnicities.AsianAmerican) {
		
			PatientDemographic memory v1 = PatientDemographic({ hasType: PatientDemographics.Overweight, exists: true });
			patient.hasDemographic[v1.hasType] = v1;
		}
	}
	
	function update(string memory newMessage) public {
		message = newMessage;
	}}