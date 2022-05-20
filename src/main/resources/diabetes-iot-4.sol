// Specifies the version of Solidity, using semantic versioning.
// Learn more: https://solidity.readthedocs.io/en/v0.5.10/layout-of-source-files.html#pragma
pragma solidity ^0.7.0;

contract DiabetesIot4 {
	string public message;
	
	constructor(string memory initMessage) {
		message = initMessage;
	}
	
	
	event RecommendDiabetesScreening(uint time);
	
	
	struct Patient {
		mapping(Recommendations => Recommendation) recommendTest;
		Ethnicity hasEthnicity;
		PatientProfile hasPatientProfile;
		mapping(DiabetesPhysicalExaminations => DiabetesPhysicalExamination) hasPhysicalExamination;
		mapping(PatientDemographics => PatientDemographic) hasDemographic;
		bool exists;
	}
	
	enum DiabetesPhysicalExaminations{ Bmi }
	
	struct DiabetesPhysicalExamination {
		DiabetesPhysicalExaminations hasType;
		int hasQuantitativeValue;
		bool exists;
	}
	
	enum Ethnicities{ HighRiskEthnicity, PacificIslander, AfricanAmerican, AsianAmerican, Latino, NativeAmerican }
	
	struct Ethnicity {
		Ethnicities hasType;
		bool exists;
	}
	
	enum PatientDemographics{ Overweight }
	
	struct PatientDemographic {
		PatientDemographics hasType;
		bool exists;
	}
	
	struct PatientProfile {
		Ethnicity hasEthnicity;
		bool exists;
	}
	
	enum Recommendations{ DiabetesScreening }
	
	struct Recommendation {
		Recommendations hasType;
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
		
		if (patient.hasPatientProfile.exists
			&& patient.hasPatientProfile.hasEthnicity.exists
			&& (patient.hasPatientProfile.hasEthnicity.hasType == Ethnicities.AfricanAmerican
			|| patient.hasPatientProfile.hasEthnicity.hasType == Ethnicities.Latino
			|| patient.hasPatientProfile.hasEthnicity.hasType == Ethnicities.NativeAmerican
			|| patient.hasPatientProfile.hasEthnicity.hasType == Ethnicities.AsianAmerican
			|| patient.hasPatientProfile.hasEthnicity.hasType == Ethnicities.PacificIslander)) {
		
			Ethnicity memory v2 = Ethnicity({ hasType: Ethnicities.HighRiskEthnicity, exists: true });
			patient.hasPatientProfile.hasEthnicity = v2;
		}
		
		if (patient.hasEthnicity.exists
			&& patient.hasEthnicity.hasType == Ethnicities.HighRiskEthnicity
			&& patient.hasDemographic[PatientDemographics.Overweight].exists) {
		
			Recommendation memory v3 = Recommendation({ hasType: Recommendations.DiabetesScreening, exists: true });
			patient.recommendTest[v3.hasType] = v3;
		
			emit RecommendDiabetesScreening(block.timestamp);
		}
	}
	
	function update(string memory newMessage) public {
		message = newMessage;
	}}