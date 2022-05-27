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
		mapping(DrugSubplans => DrugSubplan) hasPart;
		mapping(DiabetesPhysicalExaminations => DiabetesPhysicalExamination) hasPhysicalExamination;
		mapping(PatientDemographics => PatientDemographic) hasDemographic;
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
	
	enum DrugSubplans{ MonotherapyPlan }
	
	struct DrugSubplan {
		DiabetesDrug hasDrugParticipant;
		DrugSubplans hasType;
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
	
	function execute() public {
		Patient storage patient = patients[msg.sender];
	
		if (patient.hasPhysicalExamination[DiabetesPhysicalExaminations.Bmi].exists
			&& patient.hasPhysicalExamination[DiabetesPhysicalExaminations.Bmi].hasQuantitativeValue >= 35) {
		
			PatientDemographic memory v2 = PatientDemographic({ hasType: PatientDemographics.ObeseClassI, hasQuantitativeValue: 0, exists: true });
			patient.hasDemographic[v2.hasType] = v2;
		}
		
		if (patient.hasLabTest[DiabetesLaboratoryTests.Hba1c].exists
			&& patient.hasLabTest[DiabetesLaboratoryTests.Hba1c].hasQuantitativeValue >= 6
			&& patient.hasPhysicalExamination[DiabetesPhysicalExaminations.HistoryOfPrediabetes].exists
			&& patient.hasDemographic[PatientDemographics.Age].exists
			&& patient.hasDemographic[PatientDemographics.Age].hasQuantitativeValue >= 25
			&& patient.hasDemographic[PatientDemographics.Age].hasQuantitativeValue <= 59
			&& patient.hasLabTest[DiabetesLaboratoryTests.Fpg].exists
			&& patient.hasLabTest[DiabetesLaboratoryTests.Fpg].hasQuantitativeValue >= 110
			&& patient.hasDemographic[PatientDemographics.ObeseClassI].exists) {
		
			DrugSubplan memory v3 = DrugSubplan({ hasType: DrugSubplans.MonotherapyPlan, hasDrugParticipant: DiabetesDrugs.Metformin, exists: true });
			patient.hasPart[v3.hasType] = v3;
		
			emit NewTreatmentSubPlan(block.timestamp);
		}
	}
	
	function update(string memory newMessage) public {
		message = newMessage;
	}}