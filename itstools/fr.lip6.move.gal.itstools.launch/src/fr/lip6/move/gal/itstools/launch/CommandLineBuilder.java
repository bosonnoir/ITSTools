package fr.lip6.move.gal.itstools.launch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;

import fr.lip6.move.gal.BoundsProp;
import fr.lip6.move.gal.CTLProp;
import fr.lip6.move.gal.LTLProp;
import fr.lip6.move.gal.Property;
import fr.lip6.move.gal.SafetyProp;
import fr.lip6.move.gal.Specification;
import fr.lip6.move.gal.instantiate.GALRewriter;
import fr.lip6.move.gal.itstools.CommandLine;
import fr.lip6.move.gal.itstools.preference.GalPreferencesActivator;
import fr.lip6.move.gal.itstools.preference.PreferenceConstants;
import fr.lip6.move.serialization.BasicGalSerializer;
import fr.lip6.move.serialization.SerializationUtil;

public class CommandLineBuilder {

	/** Build a command line from a LaunchConfiguration 
	 * @throws CoreException */
	public static CommandLine buildCommand(ILaunchConfiguration configuration) throws CoreException {
		CommandLine cl = new CommandLine();
		
		// Path to source model file
		String oriString = configuration.getAttribute(LaunchConstants.MODEL_FILE, "model.gal");		




		// Produce a GAL file to give to its-tools
		IPath oriPath = Path.fromPortableString(oriString);

		// work folder
		File workingDirectory ;

		
		String cegarProp = configuration.getAttribute(LaunchConstants.CEGAR_PROP, "");
		if (! "".equals(cegarProp)) {
			// Path to ITS-reach exe				
			String itsReachPath = configuration.getAttribute(PreferenceConstants.ITSREACH_EXE, GalPreferencesActivator.getDefault().getPreferenceStore().getString(PreferenceConstants.ITSREACH_EXE));

			cl.addArg(itsReachPath);

			
			workingDirectory = new File (oriPath.removeLastSegments(1).toString());
			// Input file options
			cl.addArg("-i") ;
			cl.addArg(oriPath.lastSegment());
			// Model type option
			cl.addArg("-t");
			cl.addArg("CGAL");

			String doTrace = configuration.getAttribute(LaunchConstants.PLAY_TRACE, "");
			if (! "".equals(doTrace)) {
				cl.addArg("-trace");
				cl.addArg(doTrace);
			} else {
				cl.addArg("-reachable");
				cl.addArg(cegarProp);
			}
			cl.addArg("--quiet");
			
		} else {
			boolean hasCTL = false;
			boolean hasLTL = false;
			// parse it
			Specification spec = SerializationUtil.fileToGalSystem(oriString);

			// copy spec 
		//	Specification specNoProp = EcoreUtil.copy(spec);

			// clear properties : they will be fed separately
		//	specNoProp.getProperties().clear();
			// flatten it
			GALRewriter.flatten(spec, true);

			
			workingDirectory = new File (oriPath.removeLastSegments(1).append("/work/").toString());
			
			try {
				workingDirectory.mkdir();
			} catch (SecurityException e) {
				e.printStackTrace();
				throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to create work folder :"+workingDirectory+". Please check location is open to write in.",e));
			}

			String tmpPath = workingDirectory.getPath() + "/" +oriPath.lastSegment();		
			File modelff = new File(tmpPath);

			List<Property> props = new ArrayList<Property>(spec.getProperties());
			spec.getProperties().clear();
			try {
				SerializationUtil.systemToFile(spec, tmpPath);
			} catch (IOException e) {
				e.printStackTrace();
				throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to create working file :"+tmpPath+". Please check location is open to write in.",e));
			}
			for (Property p : props) {
				if (p.getBody() instanceof CTLProp) {
					hasCTL = true;
					break;
				} else if (p.getBody() instanceof LTLProp) {
					hasLTL = true;
					break;
				}
			}

			// Path to ITS-reach exe				
			String itsExePath;
			if (!hasCTL && !hasLTL) {
				itsExePath = configuration.getAttribute(PreferenceConstants.ITSREACH_EXE, GalPreferencesActivator.getDefault().getPreferenceStore().getString(PreferenceConstants.ITSREACH_EXE));
			} else if (hasCTL) {
				itsExePath = configuration.getAttribute(PreferenceConstants.ITSCTL_EXE, GalPreferencesActivator.getDefault().getPreferenceStore().getString(PreferenceConstants.ITSCTL_EXE));
			} else {
				itsExePath = configuration.getAttribute(PreferenceConstants.ITSLTL_EXE, GalPreferencesActivator.getDefault().getPreferenceStore().getString(PreferenceConstants.ITSLTL_EXE));
			}

			cl.addArg(itsExePath);

			// Input file options
			cl.addArg("-i") ;
			cl.addArg(modelff.getName());

			// Model type option
			cl.addArg("-t");
			if (spec.getMain() != null)
				cl.addArg("CGAL");
			else 
				cl.addArg("GAL");

			
			
			// test for and handle properties		
			if (! props.isEmpty()) {

				Set<String> boundvars = new LinkedHashSet<String>();
				List<Property> safeProps = new ArrayList<Property>(); 
				List<Property> ctlProps = new ArrayList<Property>(); 
				List<Property> ltlProps = new ArrayList<Property>(); 
				for (Property prop : props) {
					BasicGalSerializer bgs = new BasicGalSerializer(true);
					if (prop.getBody() instanceof BoundsProp) {
						BoundsProp bp = (BoundsProp) prop.getBody();

						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						bgs.serialize(bp.getTarget(), bos);
						String targetVar = bos.toString();
						boundvars.add(targetVar);
					} else if (prop.getBody() instanceof SafetyProp) {
						safeProps.add(prop);
					} else if (prop.getBody() instanceof CTLProp) {
						ctlProps.add(prop);
					} else if (prop.getBody() instanceof LTLProp) {
						ltlProps.add(prop);
					} 
				}
				if (!boundvars.isEmpty()) {
					boolean first=true;
					StringBuilder sb = new StringBuilder();
					for (String var : boundvars) {
						if (! first) {
							sb.append(",");
						}
						sb.append(var);
						first = false;
					}
					cl.addArg("-maxbound");
					cl.addArg(sb.toString());
				}
				if (! safeProps.isEmpty()) {
					// We will put properties in a file
					String propPath = workingDirectory.getPath() + "/" + oriPath.removeFileExtension().lastSegment() + ".prop";

					try {
						// create file
						SerializationUtil.serializePropertiesForITSTools(modelff.getName(), safeProps, propPath);

						// property file arguments
						cl.addArg("-reachable-file");
						cl.addArg(new File(propPath).getName());

					} catch (IOException e) {
						e.printStackTrace();
						throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to create file to hold properties :"+tmpPath+". Please check location is open to write in.",e));
					}
				}
				if (! ctlProps.isEmpty()) {
					// We will put properties in a file
					String propPath = workingDirectory.getPath() + "/" + oriPath.removeFileExtension().lastSegment() + ".ctl";

					try {
						// create file
						SerializationUtil.serializePropertiesForITSCTLTools(modelff.getName(), ctlProps, propPath);

						// property file arguments
						cl.addArg("-ctl");
						cl.addArg(new File(propPath).getName());

					} catch (IOException e) {
						e.printStackTrace();
						throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to create file to hold properties :"+tmpPath+". Please check location is open to write in.",e));
					}
				}
				if (! ltlProps.isEmpty()) {
					// We will put properties in a file
					String propPath = workingDirectory.getPath() + "/" + oriPath.removeFileExtension().lastSegment() + ".ltl";

					try {
						// create file
						SerializationUtil.serializePropertiesForITSLTLTools(modelff.getName(), ltlProps, propPath);

						// property file arguments
						cl.addArg("-LTL");
						cl.addArg(new File(propPath).getName());
						cl.addArg("-c");
					} catch (IOException e) {
						e.printStackTrace();
						throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to create file to hold properties :"+tmpPath+". Please check location is open to write in.",e));
					}
				}
				
				// limit verbosity
				if (!hasLTL)
					cl.addArg("--quiet");

			}
		}


		cl.setWorkingDir(workingDirectory);
		
		return cl;
	}
}
