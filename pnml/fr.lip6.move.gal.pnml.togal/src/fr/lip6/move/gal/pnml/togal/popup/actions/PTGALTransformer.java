package fr.lip6.move.gal.pnml.togal.popup.actions;

import java.util.HashMap;
import java.util.Map;
import fr.lip6.move.gal.BooleanExpression;
import fr.lip6.move.gal.ComparisonOperators;
import fr.lip6.move.gal.GALTypeDeclaration;
import fr.lip6.move.gal.GF2;
import fr.lip6.move.gal.GalFactory;
import fr.lip6.move.gal.Variable;
import fr.lip6.move.pnml.ptnet.Arc;
import fr.lip6.move.pnml.ptnet.PTMarking;
import fr.lip6.move.pnml.ptnet.Page;
import fr.lip6.move.pnml.ptnet.PetriNet;
import fr.lip6.move.pnml.ptnet.Place;
import fr.lip6.move.pnml.ptnet.PnObject;
import fr.lip6.move.pnml.ptnet.Transition;

public class PTGALTransformer {

	public GALTypeDeclaration transform(PetriNet petriNet) {

		GalFactory gf = GalFactory.eINSTANCE;

		GALTypeDeclaration gal = gf.createGALTypeDeclaration();
		gal.setName(normalizeName(petriNet.getName().getText()));

		for (Page p : petriNet.getPages()) {
			handlePage(p, gal, gf);
		}

		return gal;
	}

	private String normalizeName(String text) {
		String res = text.replace(' ', '_');
		res = res.replace('-', '_');
		return res;
	}

	private void handlePage(Page page, GALTypeDeclaration gal, GalFactory gf) {
		Map<Place, Variable> placeMap = new HashMap<Place, Variable>();
		for (PnObject n : page.getObjects()) {
			if (n instanceof Place) {
				Place p = (Place) n;

				Variable ap = GalFactory.eINSTANCE.createVariable();
				if (p.getName() != null)
					ap.setName(normalizeName(p.getName().getText()));
				else
					ap.setName(normalizeName(p.getId()));
				int value = interpretMarking(p.getInitialMarking());
				ap.setValue(GF2.constant(value));

				gal.getVariables().add(ap);
				placeMap.put(p, ap);
			}
		}

		for (PnObject pnobj : page.getObjects()) {
			if (pnobj instanceof Transition) {
				Transition t = (Transition) pnobj;
				fr.lip6.move.gal.Transition tr = gf.createTransition();
				if (t.getName() != null)
					tr.setName(normalizeName(t.getName().getText()));
				else
					tr.setName(normalizeName(t.getId()));

				BooleanExpression guard = gf.createTrue();

				for (Arc arc : t.getInArcs()) {
					Place pl = (Place) arc.getSource();
					Variable var = placeMap.get(pl);
					int value = 1;
					if (arc.getInscription() != null
							&& arc.getInscription().getText() != null) {
						value = arc.getInscription().getText();
					}
					guard = GF2.and(guard, GF2.createComparison(
							GF2.createVariableRef(var), ComparisonOperators.GE,
							GF2.constant(value)));

					tr.getActions().add(
							GF2.increment(GF2.createVariableRef(var), -value));
				}

				tr.setGuard(guard);

				for (Arc arc : t.getOutArcs()) {
					Place pl = (Place) arc.getTarget();
					Variable var = placeMap.get(pl);
					int value = 1;
					if (arc.getInscription() != null
							&& arc.getInscription().getText() != null) {
						value = arc.getInscription().getText();
					}
					tr.getActions().add(
							GF2.increment(GF2.createVariableRef(var), value));
				}
				gal.getTransitions().add(tr);

			}
		}

	}

	private int interpretMarking(PTMarking ptMarking) {
		if (ptMarking == null || ptMarking.getText() == null) {
			return 0;
		}
		return ptMarking.getText();
	}

}
