package dataAnalysis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringTokenizer;

// String string = "January 2, 2010";
// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy",
// Locale.ENGLISH);
// LocalDate date = LocalDate.parse(string, formatter);
// System.out.println(date); // 2010-01-02
//
// Calendar mydate = new GregorianCalendar();
// String mystring = "January 2, 2010";
// Date thedate = new SimpleDateFormat("MMMM d, yyyy",
// Locale.ENGLISH).parse(mystring);
// mydate.setTime(thedate);

public class User implements Serializable {
	private static final int NUMBERO_OF_NON_FILLABLE_FIELD = 6;
	private double[] homophiliaWeightValues = inizializeHomophiliaWeightValues();
	private double[] inizializeHomophiliaWeightValues() {
		double[] weights = new double[NUMBERO_OF_NON_FILLABLE_FIELD + ProfileAttributesField
				.values().length];
		for (int i = 0; i < weights.length; i++) {
			weights[i]=1;
		}
		return weights;
	}
	private long user_id;
	private AnalyzableData<Boolean> isPublicStatus;
	private double completion_percentage;
	private AnalyzableData<Boolean> isMale;
	private AnalyzableData<CharSequence> region;
	private String last_login;
	private String registration;// DateTimeFormatter
	private AnalyzableData<Integer> age;
	private AnalyzableData<Integer> heightInCm;
	private AnalyzableData<Integer> weightInKg;
	//private Pattern userParser=Pattern.compile("(\\d+)\\t([01])\\t(\\d+)\\t([01])\\t([^\\t]*)\\t([^\\t]*)\\t([^\\t]*)\\t(\\d*)(\\t([^\\t]*))+");

	//
	// array that contain user field fillable attributes
	private HomophiliaAnalysisStrategy<CharSequence> attributeStrategy=new StringValueHomophilia<CharSequence>();
	private HomophiliaAnalysisStrategy<Boolean> booleanStrategy=new NumberHomophilia<Boolean>();
	private HomophiliaAnalysisStrategy<Integer> integerStrategy=new NumberHomophilia<Integer>();


	private ArrayList<AnalyzableData<CharSequence>>[] attributes = inizializeAttributeArray();
	private ArrayList<AnalyzableData<CharSequence>>[] inizializeAttributeArray() {
		ArrayList<AnalyzableData<CharSequence>>[] arrayLists = new ArrayList[ProfileAttributesField.values().length];
		for (int i = 0; i < arrayLists.length; i++) {
			 arrayLists[i]=new ArrayList<AnalyzableData<CharSequence>>();
		}
		return arrayLists;
	}
	// ProfileAttributesField index = position in data Attributes Array you can
	// get index with ordinal():int method
	public enum ProfileAttributesField {
		I_AM_WORKING_IN_FIELD, SPOKEN_LANGUAGES, HOBBIES, I_MOST_ENJOY_GOOD_FOOD, PETS, 
		BODY_TYPE, MY_EYESIGHT, EYE_COLOR, HAIR_COLOR, HAIR_TYPE, COMPLETED_LEVEL_OF_EDUCATION, 
		FAVOURITE_COLOR, RELATION_TO_SMOKING, RELATION_TO_ALCOHOL, SIGN_IN_ZODIAC,
		ON_POKEC_I_AM_LOOKING_FOR, LOVE_IS_FOR_ME, RELATION_TO_CASUAL_SEX, MY_PARTNER_SHOULD_BE,
		MARITAL_STATUS, CHILDREN, RELATION_TO_CHILDREN, I_LIKE_MOVIES, I_LIKE_WATCHING_MOVIE,
		I_LIKE_MUSIC, I_MOSTLY_LIKE_LISTENING_TO_MUSIC, THE_IDEA_OF_GOOD_EVENING, 
		I_LIKE_SPECIALTIES_FROM_KITCHEN, FUN, I_AM_GOING_TO_CONCERTS, MY_ACTIVE_SPORTS,
		MY_PASSIVE_SPORTS, PROFESSION, I_LIKE_BOOKS, LIFE_STYLE, MUSIC, CARS, POLITICS,
		RELATIONSHIPS, ART_CULTURE, HOBBIES_INTERESTS, SCIENCE_TECHNOLOGIES, COMPUTERS_INTERNET,
		EDUCATION, SPORT, MOVIES, TRAVELLING, HEALTH, COMPANIES_BRANDS, MORE
	};

	public User() {
		isPublicStatus=new AnalyzableData<Boolean>(null, getBooleanStrategy());
		isMale=new AnalyzableData<Boolean>(null, getBooleanStrategy());
		region=new AnalyzableData<CharSequence>(null, getAttributeStrategy());
		age=new AnalyzableData<Integer>(null, getIntegerStrategy());
		heightInCm=new AnalyzableData<Integer>(null, getIntegerStrategy());
		weightInKg=new AnalyzableData<Integer>(null, getIntegerStrategy());
	}
	public double calculateHomophiliaMacth(User u) {
		double score=0;
		score +=calculateNotFreeFilledFieldHomophilia(u);
		score +=calculateUserFilledFieldHomophilia(u);
		return score;
	}

	private double calculateNotFreeFilledFieldHomophilia(User us) {
		int index=0;
		double score = homophiliaWeightValues[index]*isPublicStatus.homophiliaAnalysis(us.isPublicStatus);
		score +=homophiliaWeightValues[index]*isMale.homophiliaAnalysis(us.isMale);
		score +=homophiliaWeightValues[index]*region.homophiliaAnalysis(us.region);
		score +=homophiliaWeightValues[index]*age.homophiliaAnalysis(us.age);
		score +=homophiliaWeightValues[index]*heightInCm.homophiliaAnalysis(us.heightInCm);
		score +=homophiliaWeightValues[index]*weightInKg.homophiliaAnalysis(us.weightInKg);
		return score/NUMBERO_OF_NON_FILLABLE_FIELD;

	}

	private double calculateUserFilledFieldHomophilia(User u) {
		double score=0;
		int processedField=0;
		for (int i = NUMBERO_OF_NON_FILLABLE_FIELD; i < homophiliaWeightValues.length; i++) {
			double weight = homophiliaWeightValues[i];
			int attributeIndex = i-NUMBERO_OF_NON_FILLABLE_FIELD;
			double homophiliaUserFilledField = getHomophiliaUserFilledField(attributes[attributeIndex],u.attributes[attributeIndex]);
			score+=weight*homophiliaUserFilledField;
			processedField++;
		}

		return score/processedField;

	}

	private double getHomophiliaUserFilledField(ArrayList<AnalyzableData<CharSequence>> thisUser, ArrayList<AnalyzableData<CharSequence>> otherUser) {
		double score=0;
		int numberOfData=0;//Math.min(thisUser.size(),otherUser.size());
		
		if (numberOfData==0) {		
			return 0;
		}
		for (AnalyzableData<CharSequence> analyzableData : thisUser) {
			for (AnalyzableData<CharSequence> analyzableDataotherUser : otherUser) {
				score+=analyzableData.homophiliaAnalysis(analyzableDataotherUser);
				numberOfData++;
			}
		}
		return score/numberOfData;
	}

	


	public void parseUser(String profileTarget) {
		StringTokenizer field=new StringTokenizer(profileTarget, "	");
		StringTokenizer minorField;
		String nextToken;
		user_id=Long.parseLong(field.nextToken());
		isPublicStatus.setData(new Boolean( field.nextToken().equals("1")));
		
		isPublicStatus.setAnalyzer(getBooleanStrategy());
		completion_percentage=Integer.parseInt(field.nextToken())/100.0;
		isMale.setData(new Boolean( field.nextToken().equals("1")));
		
		isMale.setAnalyzer(getBooleanStrategy());
		region.setData(field.nextToken());
		
		region.setAnalyzer( getAttributeStrategy());
		
		last_login=field.nextToken();
		registration=field.nextToken();
		int parseInt;
		try {
			parseInt = Integer.parseInt(field.nextToken());
		} catch (NumberFormatException e) {
			parseInt=0;
		}
		age.setData(parseInt);
		age.setAnalyzer(getIntegerStrategy());
		nextToken = field.nextToken();
		minorField=new StringTokenizer(nextToken, ",");
		
		String height=null;
		Integer parseHeight=null;
		if (minorField.hasMoreTokens()) {
			height = minorField.nextToken();
			parseHeight = parseHeight(height);
		}
		heightInCm.setData(parseHeight);
		heightInCm.setAnalyzer(getIntegerStrategy());
		Integer parseWeight = null;
		if (minorField.hasMoreTokens()) {
			parseWeight = parseWeight(minorField.nextToken());
		}
		weightInKg.setData(parseWeight);
		weightInKg.setAnalyzer(getIntegerStrategy()) ;

		
		int attributeIndex = 0;
		while (field.hasMoreTokens()) {
			attributes[attributeIndex].clear();
			String attributiveField =  field.nextToken();
			if (!attributiveField.equals("null")) {
				minorField=new StringTokenizer(attributiveField, ",");
				while(minorField.hasMoreTokens()){
					String minor = minorField.nextToken();
					while (minor.startsWith(" ")&&minor.length()>1) {
						minor=minor.substring(1);
					}
					attributes[attributeIndex].add(new AnalyzableData<CharSequence>(minor, getAttributeStrategy()));
				}
			}
			attributeIndex++;
		}
	}

	private Integer parseWeight(String nextToken) {
		return removeUnit(nextToken);
	}

	private Integer parseHeight(String nextToken) {
		return removeUnit(nextToken);
	}
private Integer removeUnit(String nextToken) {
		if (nextToken.contains("null")) {
			return null;
		}else{
			try{
			return Integer.parseInt(nextToken.replaceAll(" |kg|cm", ""));
			}catch(NumberFormatException e){
				return null;
			}
		}
	}

//public static void main(String[] args) {
//	User u=new User();
//	u.parseUser("1	1	14	1	zilinsky kraj, zilina	2012-05-25 11:20:00.0	2005-04-03 00:00:00.0	26	null, null	it	anglicky	sportovanie, spanie, kino, jedlo, pocuvanie hudby, priatelia, divadlo	v dobrej restauracii	mam psa	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null");
//	System.out.println(u.user_id+" "+u.region+" "+u.weightInKg);
//	u.printAttributes();
//}

//public static void main(String[] args) {
//	User u=new User();
//	u.parseUser("32	1	72	1	zilinsky kraj, kysucke nove mesto	2012-05-13 22:52:00.0	2008-10-21 00:00:00.0	21	185 cm, 73 kg, hadam aj dobra....	ina kysuce forever....induktory a tak... :) :) :)	anglicky a slovensky.....	sex, priatelia, diskoteky, pocuvanie hudby, pozeranie filmov, tancovanie, kupalisko, stanovanie, party	kazdy kto ma pozna vie ze mi je uplne jedno kde sa najem, hlavne ze je co zjest...	mam psa, mam macku, a mam este vtacika, vsak viete akeho....	priemerna,asi tak 103 kg....akurat...:d	vyborny	hnede.......	hnede.....na kohutika... :) :) :)	rovne,hlavne rano:d	stredoskolske...mechanik elektrotechnik....bacha na mna	cervena, modra, zelena a oranzovo cierne auto...	ako kedy a ako komu... :) :) :) :)	nepijem,vzdy ma k tomu prinutia a kedze som malucky a slabucky tak sa nemozem branit... :d:d:d:d	vodnar	uz som nasiel....... :)	nie je nic lepsie, ako byt zamilovany(a)	to skutocne zalezi len na okolnostiach	moj partner by nemal byt, moj partner uz je...	mam vazny vztah,este vaznejzi ako vazny...	nemam, ale zevraj v aprili cosi bude....tak treba chodit do polska na plienky... :)	no hadam cosi pride.....	akcne, horory, komedie, serialy, dokumentarne, sci-fi, eroticke, vsetky filmy kde su pekne baby....	doma z gauca, u priatela, priatelky, a hlavne v krcme, tam viem iba zaciatok...to mi staci...	disko, hitparadovky, house, techno, rap, soundtrackyy... no bass no fun......make some noise....to su veci...:) :)	vo vani, ale pod vodou, lepsi zvuk a vacsie napatie	sadnut vecer do auta...zapnut jensena(pre tych co nevedia tak subwoofer) pustit neakeho 50centa a go to  stranske....	mamickinej a uz aj z milacikovej....:d	null	si robim koncerty doma... tuc tuc blik blik	auto-moto sporty, basketbal, kolieskove korcule, korculovanie	auto-moto sporty, bojove sporty, snowboarding	konstrukter	ktore nemusim citat....	null	null	<div> <a title=\"vstup do klubu\" href=\"/klub/milujem-rychlu-jazdu-a-pocit-slobody\">© milujem rychlu jazdu ,a pocit slobody...</a> </div>	null	null	null	null	null	null	null	null	null	null	null	null	null");
//	User u2=new User();
//	u2.parseUser("32	1	72	1	zilinsky kraj, kysucke nove mesto	2012-05-13 22:52:00.0	2008-10-21 00:00:00.0	21	185 cm, 73 kg, hadam aj dobra....	ina kysuce forever....induktory a tak... :) :) :)	anglicky a slovensky.....	sex, priatelia, diskoteky, pocuvanie hudby, pozeranie filmov, tancovanie, kupalisko, stanovanie, party	kazdy kto ma pozna vie ze mi je uplne jedno kde sa najem, hlavne ze je co zjest...	mam psa, mam macku, a mam este vtacika, vsak viete akeho....	priemerna,asi tak 103 kg....akurat...:d	vyborny	hnede.......	hnede.....na kohutika... :) :) :)	rovne,hlavne rano:d	stredoskolske...mechanik elektrotechnik....bacha na mna	cervena, modra, zelena a oranzovo cierne auto...	ako kedy a ako komu... :) :) :) :)	nepijem,vzdy ma k tomu prinutia a kedze som malucky a slabucky tak sa nemozem branit... :d:d:d:d	vodnar	uz som nasiel....... :)	nie je nic lepsie, ako byt zamilovany(a)	to skutocne zalezi len na okolnostiach	moj partner by nemal byt, moj partner uz je...	mam vazny vztah,este vaznejzi ako vazny...	nemam, ale zevraj v aprili cosi bude....tak treba chodit do polska na plienky... :)	no hadam cosi pride.....	akcne, horory, komedie, serialy, dokumentarne, sci-fi, eroticke, vsetky filmy kde su pekne baby....	doma z gauca, u priatela, priatelky, a hlavne v krcme, tam viem iba zaciatok...to mi staci...	disko, hitparadovky, house, techno, rap, soundtrackyy... no bass no fun......make some noise....to su veci...:) :)	vo vani, ale pod vodou, lepsi zvuk a vacsie napatie	sadnut vecer do auta...zapnut jensena(pre tych co nevedia tak subwoofer) pustit neakeho 50centa a go to  stranske....	mamickinej a uz aj z milacikovej....:d	null	si robim koncerty doma... tuc tuc blik blik	auto-moto sporty, basketbal, kolieskove korcule, korculovanie	auto-moto sporty, bojove sporty, snowboarding	konstrukter	ktore nemusim citat....	null	null	<div> <a title=\"vstup do klubu\" href=\"/klub/milujem-rychlu-jazdu-a-pocit-slobody\">© milujem rychlu jazdu ,a pocit slobody...</a> </div>	null	null	null	null	null	null	null	null	null	null	null	null	null");
//	u.printAttributes();
//	u2.printAttributes();
//	System.out.println(u.calculateHomophiliaMacth(u2));
//}
//public static void main(String[] args) {
	//User u=new User();
	//u.parseUser("2	1	62	0	zilinsky kraj, kysucke nove mesto	2012-05-25 23:08:00.0	2007-11-30 00:00:00.0	0	166 cm, 58 kg	null	nemecky	turistika, prace okolo domu, praca s pc, pocuvanie hudby, pozeranie filmov, tancovanie, diskoteky, kupalisko, varenie, party, priatelia, spanie, nakupovanie, stanovanie	pri svieckach s partnerom	macka	priemerna	vyborny	zelene	cierne	dlhe	zakladne, ale som uz na strednej skole dufam ze ju spravim	cierna, modra, ruzova	nefajcim	pijem prilezitostne, iba ked sa nieco kona a to napr. na zabave,na chate,na stanovackach a pod.	byk	dobreho priatela, priatelku, mozno aj viac	nie je nic lepsie, ako byt zamilovany(a)	iba s mojou laskou	laskou mojho zivota	slobodny(a)	no budu a tak chcem 2 deti staci a tak ked budeme vladat tak bude aj viac co ja viem co ma v zivote postretne:d	v buducnosti chcem mat deti	komedie, romanticke	doma z gauca	disko, pop, rap a jasn eto co teraz leti najviac nejlepsie je fun-radio	na diskoteke, pri chodzi	pri svieckach s partnerom	slovenskej	<div> <a title=\"vstup do klubu\" href=\"/klub/profesionali\">profesiona&shy;li</a> </div>	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null");
	//User u2=new User();
	//u2.parseUser("77	0	31	1	zilinsky kraj, zilina	2012-05-11 14:05:00.0	2005-08-01 00:00:00.0	23	null	studujem	null	null	null	null	priemerna	vyborny	hnede	hnede, asi..	kratke	stredoskolske	null	uz asi nie...zatial urcite nie	pijem prilezitostne	baran	null	null	null	null	null	null	null	null	null	null	null	null	null	<div> <a title=\"vstup do klubu\" href=\"/klub/vodka-jablko\">vodka &#43; jablko</a> </div>	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null	null");
	//System.out.println(">user1");
	//u.printAttributes();
	//System.out.println(">user2");
	//u2.printAttributes();
	//System.out.println(u.calculateHomophiliaMacth(u2));
//}


private String printAttributes() {
	ArrayList<AnalyzableData<CharSequence>> arrayList;
	for (int i = 0; i < attributes.length; i++) {
		arrayList=attributes[i];
		//System.out.println(arrayList.size());
		for (int j = 0; j < arrayList.size(); j++) {
			System.out.println(arrayList.get(j));
		}
	}
	return null;
}

public long getUser_id() {
	return user_id;
}

public void setUser_id(long user_id) {
	this.user_id = user_id;
}
public HomophiliaAnalysisStrategy<CharSequence> getAttributeStrategy() {
	return attributeStrategy;
}
public void setAttributeStrategy(HomophiliaAnalysisStrategy<CharSequence> attributeStrategy) {
	this.attributeStrategy = attributeStrategy;
}
public HomophiliaAnalysisStrategy<Boolean> getBooleanStrategy() {
	return booleanStrategy;
}
public void setBooleanStrategy(HomophiliaAnalysisStrategy<Boolean> booleanStrategy) {
	this.booleanStrategy = booleanStrategy;
}
public HomophiliaAnalysisStrategy<Integer> getIntegerStrategy() {
	return integerStrategy;
}
public void setIntegerStrategy(HomophiliaAnalysisStrategy<Integer> integerStrategy) {
	this.integerStrategy = integerStrategy;
}

}
