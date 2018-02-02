package mcl.persistences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import net.sourceforge.metrics.core.Metric;
import net.sourceforge.metrics.core.MetricsPlugin;
import net.sourceforge.metrics.core.sources.AbstractMetricSource;
import net.sourceforge.metrics.core.sources.Dispatcher;

@SuppressWarnings("unchecked")
public class Rule implements RuleTypes {

	private ArrayList<String> rule;
	private int ruleType;
	private String metric;
	private String metricType;
	private String source;
	private IJavaElement sourceElement;
	private int levelSource;
	private String operator;
	private double valueDesired;
	private double valueDesired2;
	private int topBottonValue;
	private double currentValue;
	private boolean isViolated = true;
	private IJavaProject jp;
	private ArrayList<IPackageFragment> packageSet;
	private double average;
	private double[] topOrBottonValues;
	
	public Rule(String[] rule, IJavaProject jp) {
		
		this.jp = jp;
		this.rule = new ArrayList<String>(Arrays.asList(rule));
		ruleType = getRuleType();
		

		if (ruleType == COMPARISON_OPERATOR) {

			source = this.rule.get(0).substring(0, this.rule.get(0).length()-1);
			metric = this.rule.get(1).toLowerCase();
			operator = this.rule.get(2);
			if (this.rule.get(3).toLowerCase().equals("average") || this.rule.get(3).toLowerCase().equals("avg")) {
				metricType = getMetricType();
				valueDesired = getAvg();
			} else {
				valueDesired = Double.parseDouble(this.rule.get(3));
			}
		}

		else if (ruleType == BETWEEN) {

			source = this.rule.get(0).substring(0, this.rule.get(0).length()-1);
			metric = this.rule.get(1).toLowerCase();
			operator = this.rule.get(2);
			if (this.rule.get(3).toLowerCase().equals("average") || this.rule.get(3).toLowerCase().equals("avg")) {
				metricType = getMetricType();
				valueDesired = getAvg();
			} else {
				valueDesired = Double.parseDouble(this.rule.get(3));
			}
			if (this.rule.get(4).toLowerCase().equals("average") || this.rule.get(3).toLowerCase().equals("avg")) {
				metricType = getMetricType();
				valueDesired2 = getAvg();
			} else {
				valueDesired2 = Double.parseDouble(this.rule.get(4));
			}

		}

		else if (ruleType == IN_TOP || ruleType == IN_BOTTON) {

			source = this.rule.get(0).substring(0, this.rule.get(0).length()-1);
			metric = this.rule.get(1).toLowerCase();
			
			operator = this.rule.get(2);
			int first = this.rule.get(3).indexOf("(");
			int last = this.rule.get(3).lastIndexOf(")");
			topBottonValue = Integer.parseInt(this.rule.get(3).substring(first + 1, last));

		}

	}

	private double getAvg() {
		try {
			double avg = 0;
			int count = 0;
			getLevelSource();
			if (levelSource == PACKAGE || levelSource == PACKAGE_SET) {
				IPackageFragment[] packages;

				packages = jp.getPackageFragments();

				for (IPackageFragment pckg : packages) {
					if(pckg.getKind() == IPackageFragmentRoot.K_SOURCE && !pckg.isDefaultPackage()){
						avg += getMetric(pckg.getPrimaryElement(), metricType);
						count++;
					}
				}
			}

			else if (levelSource == CLASS) {

				IPackageFragment[] packages = jp.getPackageFragments();

				for (IPackageFragment pckg : packages) {
					if(pckg.getKind() == IPackageFragmentRoot.K_SOURCE && !pckg.isDefaultPackage()){
						ICompilationUnit[] compilationUnits = pckg.getCompilationUnits();
						for (ICompilationUnit u : compilationUnits) {
							IType[] types = u.getAllTypes();
							for (IType p : types) {
								avg += getMetric(p.getPrimaryElement(), metricType);
								count++;
							}
	
						}
					}
				}
			}

			else if (levelSource == METHOD) {
				IPackageFragment[] packages = jp.getPackageFragments();

				for (IPackageFragment pckg : packages) {
					if(pckg.getKind() == IPackageFragmentRoot.K_SOURCE && !pckg.isDefaultPackage()){
						ICompilationUnit[] compilationUnits = pckg.getCompilationUnits();
						for (ICompilationUnit u : compilationUnits) {
							IType[] types = u.getAllTypes();
							for (IType p : types) {
								IMethod[] methods = p.getMethods();
								for (IMethod method : methods) {
									avg += getMetric(method.getPrimaryElement(), metricType);
									count++;
								}
	
							}
	
						}
					}
				}

			}
			
			average = avg / count;
			return avg / count;

		} catch (JavaModelException e) {
			e.printStackTrace();
			average = 0;
			return 0;
		}
	}

	@SuppressWarnings("rawtypes")
	public boolean isViolated() {

		getLevelSource();
		metricType = getMetricType();

		if (levelSource == PACKAGE_SET) {
			isViolated = performPackageSetChecker();
		}

		else if (ruleType == COMPARISON_OPERATOR) {

			currentValue = getMetric(sourceElement, metricType);

			if (operator.compareTo("<") == 0) {
				if (currentValue < valueDesired) {
					isViolated = false;
				}
			} else if (operator.compareTo("<=") == 0) {
				if (currentValue <= valueDesired) {
					isViolated = false;
				}
			} else if (operator.compareTo("=") == 0) {
				if (currentValue == valueDesired) {
					isViolated = false;
				}
			}

			else if (operator.compareTo("!=") == 0) {
				if (currentValue != valueDesired) {
					isViolated = false;
				}
			}

			else if (operator.compareTo(">") == 0) {
				if (currentValue > valueDesired) {
					isViolated = false;
				}
			} else if (operator.compareTo(">=") == 0) {
				if (currentValue >= valueDesired) {
					isViolated = false;
				}
			}

		}

		else if (ruleType == BETWEEN) {
			currentValue = getMetric(sourceElement, metricType);
			if (currentValue > valueDesired && currentValue < valueDesired2) {
				isViolated = false;
			}
		}

		else if (ruleType == IN_TOP || ruleType == IN_BOTTON) {
			
			currentValue = getMetric(sourceElement, metricType);
			
			if (levelSource == PACKAGE) {

				try {
					ArrayList<SourceMetric> sm = new ArrayList<SourceMetric>();

					IPackageFragment[] packages;

					packages = jp.getPackageFragments();

					for (IPackageFragment pckg : packages) {
						if(pckg.getKind() == IPackageFragmentRoot.K_SOURCE && !pckg.isDefaultPackage()){
							sm.add(new SourceMetric(pckg.getElementName(),
									getMetric(pckg.getPrimaryElement(), metricType)));
						}
					}

					if (ruleType == IN_TOP) {

						Collections.sort(sm, new Comparator() {
							public int compare(Object o1, Object o2) {
								SourceMetric m1 = (SourceMetric) o1;
								SourceMetric m2 = (SourceMetric) o2;
								return m1.getMetric() > m2.getMetric() ? -1
										: (m1.getMetric() < m2.getMetric() ? +1 : 0);
							}
						});
						
						double[] topValues = {sm.get(0).getMetric(),sm.get(topBottonValue-1).getMetric()};
						topOrBottonValues = topValues;

						for (int i = 0; i < topBottonValue; i++) {
							if (sm.get(i).getSource().equals(source)) {
								isViolated = false;
								break;
							}
						}
					}

					if (ruleType == IN_BOTTON) {

						Collections.sort(sm, new Comparator() {
							public int compare(Object o1, Object o2) {
								SourceMetric m1 = (SourceMetric) o1;
								SourceMetric m2 = (SourceMetric) o2;
								return m1.getMetric() < m2.getMetric() ? -1
										: (m1.getMetric() > m2.getMetric() ? +1 : 0);
							}
						});
						
						double[] bottonValues = {sm.get(0).getMetric(),sm.get(topBottonValue-1).getMetric()};
						topOrBottonValues = bottonValues;

						for (int i = 0; i < topBottonValue; i++) {
							if (sm.get(i).getSource().equals(source)) {
								isViolated = false;
								break;
							}
						}

					}

				} catch (JavaModelException e) {
					e.printStackTrace();
				}
			}

			else if (levelSource == CLASS) {
				try {
					ArrayList<SourceMetric> sm = new ArrayList<SourceMetric>();

					IPackageFragment[] packages = jp.getPackageFragments();

					for (IPackageFragment pckg : packages) {
						if(pckg.getKind() == IPackageFragmentRoot.K_SOURCE && !pckg.isDefaultPackage()){
							ICompilationUnit[] compilationUnits = pckg.getCompilationUnits();
							for (ICompilationUnit u : compilationUnits) {
								IType[] types = u.getAllTypes();
								for (IType p : types) {
									sm.add(new SourceMetric(p.getFullyQualifiedName(),
											getMetric(p.getPrimaryElement(), metricType)));
								}
	
							}
						}
					}

					if (ruleType == IN_TOP) {

						Collections.sort(sm, new Comparator() {
							public int compare(Object o1, Object o2) {
								SourceMetric m1 = (SourceMetric) o1;
								SourceMetric m2 = (SourceMetric) o2;
								return m1.getMetric() > m2.getMetric() ? -1
										: (m1.getMetric() < m2.getMetric() ? +1 : 0);
							}
						});
						
						double[] topValues = {sm.get(0).getMetric(),sm.get(topBottonValue-1).getMetric()};
						topOrBottonValues = topValues;

						for (int i = 0; i < topBottonValue; i++) {
							if (sm.get(i).getSource().equals(source)) {
								isViolated = false;
								break;
							}
						}
					}

					if (ruleType == IN_BOTTON) {

						Collections.sort(sm, new Comparator() {
							public int compare(Object o1, Object o2) {
								SourceMetric m1 = (SourceMetric) o1;
								SourceMetric m2 = (SourceMetric) o2;
								return m1.getMetric() < m2.getMetric() ? -1
										: (m1.getMetric() > m2.getMetric() ? +1 : 0);
							}
						});
						
						double[] bottonValues = {sm.get(0).getMetric(),sm.get(topBottonValue-1).getMetric()};
						topOrBottonValues = bottonValues;

						for (int i = 0; i < topBottonValue; i++) {
							if (sm.get(i).getSource().equals(source)) {
								isViolated = false;
								break;
							}
						}

					}

				} catch (JavaModelException e) {
					e.printStackTrace();
				}
			}

			else if (levelSource == METHOD) {
				try {
					ArrayList<SourceMetric> sm = new ArrayList<SourceMetric>();

					IPackageFragment[] packages = jp.getPackageFragments();

					for (IPackageFragment pckg : packages) {
						if(pckg.getKind() == IPackageFragmentRoot.K_SOURCE && !pckg.isDefaultPackage()){
							ICompilationUnit[] compilationUnits = pckg.getCompilationUnits();
							for (ICompilationUnit u : compilationUnits) {
								IType[] types = u.getAllTypes();
								for (IType p : types) {
									IMethod[] methods = p.getMethods();
									for (IMethod method : methods) {
										sm.add(new SourceMetric(getMethodQualifiedName(method),
												getMetric(method.getPrimaryElement(), metricType)));
									}
	
								}
	
							}
						}
					}

					if (ruleType == IN_TOP) {

						Collections.sort(sm, new Comparator() {
							public int compare(Object o1, Object o2) {
								SourceMetric m1 = (SourceMetric) o1;
								SourceMetric m2 = (SourceMetric) o2;
								return m1.getMetric() > m2.getMetric() ? -1
										: (m1.getMetric() < m2.getMetric() ? +1 : 0);
							}
						});
						
						double[] topValues = {sm.get(0).getMetric(),sm.get(topBottonValue-1).getMetric()};
						topOrBottonValues = topValues;

						for (int i = 0; i < topBottonValue; i++) {
							if (sm.get(i).getSource().equals(source)) {
								isViolated = false;
								break;
							}
						}
					}

					if (ruleType == IN_BOTTON) {

						Collections.sort(sm, new Comparator() {
							public int compare(Object o1, Object o2) {
								SourceMetric m1 = (SourceMetric) o1;
								SourceMetric m2 = (SourceMetric) o2;
								return m1.getMetric() < m2.getMetric() ? -1
										: (m1.getMetric() > m2.getMetric() ? +1 : 0);
							}
						});
						
						double[] bottonValues = {sm.get(0).getMetric(),sm.get(topBottonValue-1).getMetric()};
						topOrBottonValues = bottonValues;

						for (int i = 0; i < topBottonValue; i++) {
							if (sm.get(i).getSource().equals(source)) {
								isViolated = false;
								break;
							}
						}

					}

				} catch (JavaModelException e) {
					e.printStackTrace();
				}
			}
		}

		return isViolated;
	}
	
	private String getMethodQualifiedName(IMethod method){
		String qualifiedName = method.getDeclaringType().getFullyQualifiedName()+"."+method.getElementName();
		try {
			ILocalVariable[] parametersMethod = method.getParameters();
			
			if(parametersMethod == null || parametersMethod.length == 0){
				qualifiedName = qualifiedName+"()";
			}
			else{
				qualifiedName = qualifiedName+"(";
				for (int i = 0; i < parametersMethod.length; i++) {
					
					if(i == (parametersMethod.length)-1){
						String typeParameter;
						if(parametersMethod[i].getTypeSignature().contains("Z")){
							typeParameter = "boolean";
						}
						else if(parametersMethod[i].getTypeSignature().contains("B")){
							typeParameter = "byte";
						}
						else if(parametersMethod[i].getTypeSignature().contains("C")){
							typeParameter = "char";
						}
						else if(parametersMethod[i].getTypeSignature().contains("S")){
							typeParameter = "short";
						}
						else if(parametersMethod[i].getTypeSignature().contains("I")){
							typeParameter = "int";
						}
						else if(parametersMethod[i].getTypeSignature().contains("J")){
							typeParameter = "long";
						}
						else if(parametersMethod[i].getTypeSignature().contains("F")){
							typeParameter = "float";
						}
						else if(parametersMethod[i].getTypeSignature().contains("D")){
							typeParameter = "double";
						}
						else{
							typeParameter = parametersMethod[i].getTypeSignature().substring(1, parametersMethod[i].getTypeSignature().length()-1);

						}
						
						qualifiedName = qualifiedName+typeParameter+")";
					}
					
					else{
						String typeParameter = parametersMethod[i].getTypeSignature().substring(1, parametersMethod[i].getTypeSignature().length()-1);
						qualifiedName = qualifiedName+typeParameter+", ";
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		
		return qualifiedName;
	}

	@SuppressWarnings("rawtypes")
	private boolean performPackageSetChecker() {

		boolean isViolated = true;

		if (ruleType == COMPARISON_OPERATOR) {

			for (IPackageFragment packageFragment : packageSet) {

				currentValue = getMetric(packageFragment.getPrimaryElement(), metricType);

				if (operator.compareTo("<") == 0) {
					if (currentValue < valueDesired) {
						isViolated = false;
					}
				} else if (operator.compareTo("<=") == 0) {
					if (currentValue <= valueDesired) {
						isViolated = false;
					}
				} else if (operator.compareTo("=") == 0) {
					if (currentValue == valueDesired) {
						isViolated = false;
					}
				}

				else if (operator.compareTo("!=") == 0) {
					if (currentValue != valueDesired) {
						isViolated = false;
					}
				}

				else if (operator.compareTo(">") == 0) {
					if (currentValue > valueDesired) {
						isViolated = false;
					}
				} else if (operator.compareTo(">=") == 0) {
					if (currentValue >= valueDesired) {
						isViolated = false;
					}
				}
			}

		} else if (ruleType == BETWEEN) {

			for (IPackageFragment packageFragment : packageSet) {
				currentValue = getMetric(packageFragment.getPrimaryElement(), metricType);
				if (currentValue > valueDesired && currentValue < valueDesired2) {
					isViolated = false;
				}
				else{
					isViolated = true;
					break;
				}
			}
		}

		else if (ruleType == IN_TOP || ruleType == IN_BOTTON) {

			try {
				
				currentValue = getMetric(sourceElement, metricType);
				
				ArrayList<SourceMetric> sm = new ArrayList<SourceMetric>();
				IPackageFragment[] packages;
				packages = jp.getPackageFragments();

				for (IPackageFragment pckg : packages) {
					if(pckg.getKind() == IPackageFragmentRoot.K_SOURCE && !pckg.isDefaultPackage()){
						sm.add(new SourceMetric(pckg.getElementName(), getMetric(pckg.getPrimaryElement(), metricType)));
					}
				}

				if (ruleType == IN_TOP) {

					Collections.sort(sm, new Comparator() {
						public int compare(Object o1, Object o2) {
							SourceMetric m1 = (SourceMetric) o1;
							SourceMetric m2 = (SourceMetric) o2;
							return m1.getMetric() > m2.getMetric() ? -1 : (m1.getMetric() < m2.getMetric() ? +1 : 0);
						}
					});

					for (IPackageFragment packageFragment : packageSet) {
						for (int i = 0; i < topBottonValue; i++) {
							if (sm.get(i).getSource().equals(packageFragment.getElementName())) {
								isViolated = false;
							}
							else{
								isViolated = true;
								break;
							}
						}
					}
				}

				if (ruleType == IN_BOTTON) {

					Collections.sort(sm, new Comparator() {
						public int compare(Object o1, Object o2) {
							SourceMetric m1 = (SourceMetric) o1;
							SourceMetric m2 = (SourceMetric) o2;
							return m1.getMetric() < m2.getMetric() ? -1 : (m1.getMetric() > m2.getMetric() ? +1 : 0);
						}
					});

					for (IPackageFragment packageFragment : packageSet) {
						for (int i = 0; i < topBottonValue; i++) {
							if (sm.get(i).getSource().equals(packageFragment.getElementName())) {
								isViolated = false;
							}
							else{
								isViolated = true;
								break;
							}
						}
					}
				}

			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}

		return isViolated;
	}

	private int getRuleType() {

		// condição para operadores >, <, =, <=, >=, !=
		if (rule.size() == 4) {
			// in
			if (rule.get(2).equals("in")) {

				// top values
				if (rule.get(3).toLowerCase().contains("topvalues")) {
					return IN_TOP;
				}

				// botton values
				else if (rule.get(3).toLowerCase().contains("bottomvalues")) {
					return IN_BOTTON;
				}
			} else {
				return COMPARISON_OPERATOR;
			}
		}

		// condição para operador between
		if (rule.size() == 5) {

			// between
			if (rule.get(2).toLowerCase().equals("between")) {
				return BETWEEN;
			}

		}

		return -1;

	}

	private String getMetricType() {

		String metricName = "";

		// McCabe Cyclomatic Complexity (VG)
		if (metric.compareTo("mccabe") == 0 || metric.compareTo("vg") == 0) {
			metricName = "VG";
		}

		// Number of Parameters (PAR)
		else if (metric.compareTo("par") == 0 || metric.compareTo("params") == 0) {
			metricName = "PAR";
		}

		// Nested Block Depth (NBD)
		else if (metric.compareTo("nbd") == 0 || metric.compareTo("nestedblockdepth") == 0) {
			metricName = "NBD";
		}

		// Afferent Coupling (CA)
		else if (metric.compareTo("ca") == 0) {
			metricName = "CA";
		}

		// Efferent Coupling (CE)
		else if (metric.compareTo("ce") == 0) {
			metricName = "CE";
		}

		// Instability (RMI)
		else if (metric.compareTo("rmi") == 0 || metric.toLowerCase().equals("instability")) {
			metricName = "RMI";
		}

		// Abstractness (RMA)
		else if (metric.compareTo("rma") == 0) {
			metricName = "RMA";
		}

		// Normalized Distance (RMD)
		else if (metric.compareTo("rmd") == 0) {
			metricName = "RMD";
		}

		// Depth of Inheritance Tree (DIT)
		else if (metric.compareTo("dit") == 0) {
			metricName = "DIT";
		}

		// Weighted Methods per Class (WMC)
		else if (metric.compareTo("wmc") == 0) {
			metricName = "WMC";
		}

		// Number of Children (NSC)
		else if (metric.compareTo("nsc") == 0) {
			metricName = "NSC";
		}

		// Number of Overridden Methods (NORM)
		else if (metric.compareTo("norm") == 0) {
			metricName = "NORM";
		}

		// Lack of Cohesion of Methods (LCOM)
		else if (metric.compareTo("lcom") == 0) {
			metricName = "LCOM";
		}

		// Number of Attributes (NOF)
		else if (metric.compareTo("nof") == 0) {
			metricName = "NOF";
		}

		// Number of Static Attributes/Fields (NSF)
		else if (metric.compareTo("nsf") == 0) {
			metricName = "NSF";
		}

		// Number of Methods (NOM)
		else if (metric.compareTo("nom") == 0) {
			metricName = "NOM";
		}

		// Number of Normal Methods - excludes getters and setters (NONM)
		else if (metric.compareTo("nonm") == 0) {
			metricName = "NONM";
		}

		// Number of Inherited Methods (NMI)
		else if (metric.compareTo("nmi") == 0) {
			metricName = "NMI";
		}

		// Number of Static Methods (NSM)
		else if (metric.compareTo("nsm") == 0) {
			metricName = "NSM";
		}

		// Specialization Index (SIX)
		else if (metric.compareTo("six") == 0) {
			metricName = "SIX";
		}

		// Specialization Index 2 (SIX2)
		else if (metric.compareTo("six2") == 0) {
			metricName = "SIX2";
		}

		// Number of Classes (NOC)
		else if (metric.compareTo("noc") == 0) {
			metricName = "NOC";
		}

		// Number of Interfaces (NOI)
		else if (metric.compareTo("noi") == 0) {
			metricName = "NOI";
		}

		// Number of Packages (NOP)
		else if (metric.compareTo("nop") == 0) {
			metricName = "NOP";
		}

		// Total Lines of Code (TLOC)
		else if (metric.compareTo("tloc") == 0) {
			metricName = "TLOC";
		}

		// New Methods Line of Code (MLOC)
		else if (metric.compareTo("mloc") == 0) {
			metricName = "MLOC";
		}

		// Design Size in Classes (DSC) - Equivalent to the NUM_TYPES (NOC)
		// metric
		else if (metric.compareTo("dsc") == 0) {
			metricName = "DSC";
		}

		// Average Number of Ancestors (ANA)
		else if (metric.compareTo("ana") == 0) {
			metricName = "ANA";
		}

		// Measure of Functional Abstraction (MFA)
		else if (metric.compareTo("mfa") == 0) {
			metricName = "MFA";
		}

		// Class Interface Size (CIS)
		else if (metric.compareTo("cis") == 0) {
			metricName = "CIS";
		}

		// Measure of Aggregation (MOA)
		else if (metric.compareTo("moa") == 0) {
			metricName = "MOA";
		}

		// Data Access Metric (DAM)
		else if (metric.compareTo("dam") == 0) {
			metricName = "DAM";
		}

		// Number of Polymorhic Methods (NOPM)
		else if (metric.compareTo("nopm") == 0) {
			metricName = "NOPM";
		}

		// Cohesion Among Methods of Class (CAM)
		else if (metric.compareTo("cam") == 0) {
			metricName = "CAM";
		}

		// Direct Class Coupling (DCC)
		else if (metric.compareTo("dcc") == 0) {
			metricName = "DCC";
		}

		// Reusability (REU)
		else if (metric.compareTo("reusability") == 0 || metric.compareTo("reu") == 0) {
			metricName = "REU";
		}

		// Flexibility (FLE)
		else if (metric.compareTo("fle") == 0 || metric.compareTo("flexibility") == 0) {
			metricName = "FLE";
		}

		// Effectiveness (EFE)
		else if (metric.compareTo("efe") == 0 || metric.compareTo("effectiveness") == 0) {
			metricName = "EFE";
		}

		// Extendibility (EXT)
		else if (metric.compareTo("ext") == 0 || metric.compareTo("extendibility") == 0) {
			metricName = "EXT";
		}

		// Functionality (FUN)
		else if (metric.compareTo("fun") == 0 || metric.compareTo("functionality") == 0) {
			metricName = "FUN";
		}

		// Understandability (ENT ou UND)
		else if (metric.compareTo("ent") == 0 || metric.compareTo("und") == 0
				|| metric.compareTo("understandability") == 0) {
			metricName = "ENT";
		}

		// Number of Hierarchies (NOH)
		else if (metric.compareTo("noh") == 0) {
			metricName = "NOH";
		}

		return metricName;
	}

	private void getLevelSource() {
		// String levelSourceName = "";
		try {

			// Verifica se eh projeto
			if (source.compareTo("project") == 0 || source.compareTo(jp.getElementName()) == 0 || source.toLowerCase().equals("system") ) {
				// levelSourceName = "project";
				sourceElement = jp.getPrimaryElement();
				levelSource = PROJECT;
				return;
				// return jp.getPrimaryElement();
			}

			// Verifica se eh conjunto de pacotes
			else if (source.contains("*")) {
				levelSource = PACKAGE_SET;
				int limit = source.indexOf("*");
				String packageSetName = source.substring(0, limit);
				IPackageFragment[] packages = jp.getPackageFragments();

				for (IPackageFragment pckg : packages) {
					if(pckg.getKind() == IPackageFragmentRoot.K_SOURCE && !pckg.isDefaultPackage()){
						if (pckg.getElementName().contains(packageSetName)) {
							// levelSourceName = "package";
	
							if (packageSet == null) {
								packageSet = new ArrayList<IPackageFragment>();
							}
							packageSet.add(pckg);
	
							// return pckg.getPrimaryElement();
						}
					}
				}

				return;
			}

			// Verifica se eh pacote
			IPackageFragment[] packages = jp.getPackageFragments();

			for (IPackageFragment pckg : packages) {
				if(pckg.getKind() == IPackageFragmentRoot.K_SOURCE && !pckg.isDefaultPackage()){
					if (pckg.getElementName().compareTo(source) == 0) {
						// levelSourceName = "package";
						sourceElement = pckg.getPrimaryElement();
						levelSource = PACKAGE;
						return;
						// return pckg.getPrimaryElement();
					}
			}
			}

			// Verifica se eh classe
			IType type = jp.findType(source);
			if (type != null) {
				// levelSourceName = "class";
				sourceElement = type.getPrimaryElement();
				levelSource = CLASS;
				return;
				// return type.getPrimaryElement();
			}

			// Vefirica se eh metodo
			int index = source.lastIndexOf(".");
			String className = source.substring(0, index);
			int index2 = source.indexOf("(");
			String methodName = source.substring(index + 1, index2);
			int index3 = source.lastIndexOf(")");
			String[] parameters = source.substring(index2 + 1, index3).split(",");

			IMethod[] methods = jp.findType(className).getMethods();

			for (IMethod method : methods) {

				if (method.getElementName().compareTo(methodName) == 0) {
					if (method.getNumberOfParameters() == parameters.length) {
						ILocalVariable[] parametersMethod = method.getParameters();
						boolean todosBatem = true;
						String typeParameter;
						for (int i = 0; i < method.getNumberOfParameters(); i++) {
							if(parametersMethod[i].getTypeSignature().contains("Z")){
								typeParameter = "boolean";
							}
							else if(parametersMethod[i].getTypeSignature().contains("B")){
								typeParameter = "byte";
							}
							else if(parametersMethod[i].getTypeSignature().contains("C")){
								typeParameter = "char";
							}
							else if(parametersMethod[i].getTypeSignature().contains("S")){
								typeParameter = "short";
							}
							else if(parametersMethod[i].getTypeSignature().contains("I")){
								typeParameter = "int";
							}
							else if(parametersMethod[i].getTypeSignature().contains("J")){
								typeParameter = "long";
							}
							else if(parametersMethod[i].getTypeSignature().contains("F")){
								typeParameter = "float";
							}
							else if(parametersMethod[i].getTypeSignature().contains("D")){
								typeParameter = "double";
							}
							else{
								typeParameter = parametersMethod[i].getTypeSignature().substring(1, parametersMethod[i].getTypeSignature().length()-1);

							}
							
							if (typeParameter.compareTo(parameters[i]) != 0) {
								todosBatem = false;
							}
						}

						if (todosBatem) {
							// levelSourceName = "method";
							sourceElement = method.getPrimaryElement();
							levelSource = METHOD;
							return;
							// return method.getPrimaryElement();

						}

					}

				}
			}

		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		// return null;
	}

	private double getMetric(IJavaElement sourceElement, String metric) {

		double metricValue = -1;

		try {

			AbstractMetricSource ms = Dispatcher.getAbstractMetricSource(sourceElement);

			if (ms == null) {
				return metricValue;
			}

			MetricsPlugin plugin = MetricsPlugin.getDefault();
			String[] names = plugin.getMetricIds();
			for (int i = 0; i < names.length; i++) {

				if (names[i].matches(metric)) {

					Metric m = ms.getValue(names[i]);
					metricValue = m.getValue();
					break;
				}
			}

		} catch (Throwable e) {
			// TODO verificar se, quando da nulo, eh pq a metrica não eh de
			// projeto/pacote/classe (por enquanto retorna 0)
			return -1;
			// Log.logError("MetricsTable::setMetrics", e);
		}

		return metricValue;
	}

	public String getRule() {
		String aux = "";
		
		for(int i=0; i<rule.size(); i++){
			aux = aux.concat(rule.get(i))+" ";
		}
		return aux;
	}

	public double getCurrentMetricValue() {
		return currentValue;
	}

	public double getAverage(){
		return average;
	}
	
	public double[] getTopOrBottonValues(){
		return topOrBottonValues;
	}
	
	
	class SourceMetric {

		private String source;
		private double metric;

		public SourceMetric(String source, double metric) {

			this.source = source;
			this.metric = metric;
		}

		public String getSource() {
			return source;
		}

		public double getMetric() {
			return metric;
		}

	}
}
