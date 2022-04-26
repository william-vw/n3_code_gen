// Specifies the version of Solidity, using semantic versioning.
// Learn more: https://solidity.readthedocs.io/en/v0.5.10/layout-of-source-files.html#pragma
pragma solidity ^0.7.0;

contract DiabetesIot {
	// Declares a state variable `message` of type `string`.
	// State variables are variables whose values are permanently stored in contract storage. The keyword `public` makes variables accessible from outside a contract and creates a function that other contracts or clients can call to access the value.
	string public message;
	
	// Similar to many class-based object-oriented languages, a constructor is a special function that is only executed upon contract creation.
	// Constructors are used to initialize the contract's data. Learn more:https://solidity.readthedocs.io/en/v0.5.10/contracts.html#constructors
	constructor(string memory initMessage) {
	
		// Accepts a string argument `initMessage` and sets the value into the contract's `message` storage variable).
		message = initMessage;
	}
	
	function containsWithType(list, el) {
		for (uint i = 0; i < list.length; i++) {
			if (el == list[i].type) {
				return true;
			}
		}
		return false;
	}
	
	enum TreatmentSubplanConstants{ LifestyleSubplan }
	
	struct TreatmentSubplan {
		string label;
		TreatmentSubplanConstants type;
	}
	
	enum DiabetesPhysicalExaminationConstants{ Bmi }
	
	struct DiabetesPhysicalExamination {
		PatientProfile isPhysicalExaminationOf;
		DiabetesPhysicalExaminationConstants type;
		int hasQuantitativeValue;
	}
	
	enum DiabetesMellitusConstants{ Type2DiabetesMellitus }
	
	struct DiabetesMellitus {
		DiabetesMellitusConstants type;
	}
	
	struct TreatmentPlan {
		TreatmentSubplan[] hasPart;
		TreatmentPlanConstants type;
	}
	
	struct DiabetesDiagnosis {
		DiabetesMellitus hasDiabetesType;
		DiabetesDiagnosisConstants type;
	}
	
	struct Patient {
		PatientProfile hasPatientProfile;
		PatientConstants type;
	}
	
	struct PatientProfile {
		Patient isPatientProfileOf;
		PatientDemographic[] hasDemographic;
		TreatmentPlan hasTreatmentPlan;
		DiabetesDiagnosis hasDiagnosis;
		PatientProfileConstants type;
	}
	
	enum PatientDemographicConstants{ Overweight }
	
	struct PatientDemographic {
		PatientDemographicConstants type;
	}
	
	function doSomething(exam, p) {
		if (exam.hasQuantitativeValue != false
			&& exam.hasQuantitativeValue >= 25
			&& exam.type == DiabetesPhysicalExaminationConstants.Bmi) {
		
			PatientDemographic memory v0 = PatientDemographic();
			exam.isPhysicalExaminationOf.hasDemographic.push(v0);
			
			v0.type = PatientDemographicConstants.Overweight;
		}
		
		if (p.hasPatientProfile != false
			&& p.hasPatientProfile.hasDiagnosis != false
			&& p.hasPatientProfile.hasDiagnosis.hasDiabetesType != false
			&& p.hasPatientProfile.hasDiagnosis.hasDiabetesType.type == DiabetesMellitusConstants.Type2DiabetesMellitus
			&& containsWithType(p.hasPatientProfile.hasDemographic, PatientDemographicConstants.Overweight)) {
		
			if (p.hasPatientProfile.hasTreatmentPlan == false) {
				p.hasPatientProfile.hasTreatmentPlan = TreatmentPlan();
			}
			TreatmentPlan memory v1 = p.hasPatientProfile.hasTreatmentPlan;
			
			TreatmentSubplan memory v2 = TreatmentSubplan();
			v1.hasPart.push(v2);
			
			v2.label = "Management and reduction of weight is important";
			v2.type = TreatmentSubplanConstants.LifestyleSubplan;
		}
	}
	
	// A public function that accepts a string argument and updates the `message` storage variable.
	function update(string memory newMessage) public {
		message = newMessage;
	}}