// Specifies the version of Solidity, using semantic versioning.
// Learn more: https://solidity.readthedocs.io/en/v0.5.10/layout-of-source-files.html#pragma
pragma solidity ^0.7.0;

contract DiabetesIot2 {
	string public message;
	
	constructor(string memory initMessage) {
		message = initMessage;
	}
	
	
	event NewTreatmentSubPlan(uint time);
	
	
	struct Patient {
		PatientProfile hasPatientProfile;
		bool exists;
	}
	
	struct PatientProfile {
		mapping(DiabetesPhysicalExaminations => DiabetesPhysicalExamination) hasPhysicalExamination;
		mapping(PatientDemographics => PatientDemographic) hasDemographic;
		TreatmentPlan hasTreatmentPlan;
		mapping(DiabetesLaboratoryTests => DiabetesLaboratoryTest) hasLabTest;
		bool exists;
	}
	
	enum DiabetesPhysicalExaminations{ Bmi, HistoryOfPrediabetes }
	
	struct DiabetesPhysicalExamination {
		DiabetesPhysicalExaminations hasType;
		int hasQuantitativeValue;
		bool exists;
	}
	
	enum PatientDemographics{ Age, ObeseClassI }
	
	struct PatientDemographic {
		PatientDemographics hasType;
		int hasQuantitativeValue;
		bool exists;
	}
	
	struct TreatmentPlan {
		mapping(DrugSubplans => DrugSubplan) hasPart;
		bool exists;
	}
	
	struct DrugSubplan {
		DiabetesDrug hasDrugParticipant;
		bool exists;
	}
	
	enum DiabetesDrugs{ Metformin }
	
	struct DiabetesDrug {
		DiabetesDrugs hasType;
		bool exists;
	}
	
	enum DiabetesLaboratoryTests{ Hba1c, Fpg }
	
	struct DiabetesLaboratoryTest {
		DiabetesLaboratoryTests hasType;
		int hasQuantitativeValue;
		bool exists;
	}
	
	
	mapping(address => Patient) patients;
	
	function execute() {
		Patient storage patient = patients[msg.sender];
	
		if (patient.hasPatientProfile.exists
			&& patient.hasPatientProfile.hasPhysicalExamination[DiabetesPhysicalExaminations.Bmi].exists
			&& patient.hasPatientProfile.hasPhysicalExamination[DiabetesPhysicalExaminations.Bmi].hasQuantitativeValue >= 35) {
		
			PatientDemographic memory v0 = PatientDemographic({ hasType: PatientDemographics.ObeseClassI, exists: true });
			patient.hasPatientProfile.hasDemographic[v0.hasType] = v0;
		}
		
		if (patient.hasPatientProfile.exists
			&& patient.hasPatientProfile.hasTreatmentPlan.exists
			&& patient.hasPatientProfile.hasLabTest[DiabetesLaboratoryTests.Hba1c].exists
			&& patient.hasPatientProfile.hasLabTest[DiabetesLaboratoryTests.Hba1c].hasQuantitativeValue >= 6
			&& patient.hasPatientProfile.hasPhysicalExamination[DiabetesPhysicalExaminations.HistoryOfPrediabetes].exists
			&& patient.hasPatientProfile.hasDemographic[PatientDemographics.Age].exists
			&& patient.hasPatientProfile.hasDemographic[PatientDemographics.Age].hasQuantitativeValue >= 25
			&& patient.hasPatientProfile.hasDemographic[PatientDemographics.Age].hasQuantitativeValue <= 59
			&& patient.hasPatientProfile.hasLabTest[DiabetesLaboratoryTests.Fpg].exists
			&& patient.hasPatientProfile.hasLabTest[DiabetesLaboratoryTests.Fpg].hasQuantitativeValue >= 110
			&& patient.hasPatientProfile.hasDemographic[PatientDemographics.ObeseClassI].exists) {
		
			DrugSubplan memory v1 = DrugSubplan({ hasDrugParticipant: DiabetesDrugs.Metformin, exists: true });
			patient.hasPatientProfile.hasTreatmentPlan.hasPart[v1.hasType] = v1;
		
			emit NewTreatmentSubPlan(block.timestamp);
		}
	}
	
	function update(string memory newMessage) public {
		message = newMessage;
	}}