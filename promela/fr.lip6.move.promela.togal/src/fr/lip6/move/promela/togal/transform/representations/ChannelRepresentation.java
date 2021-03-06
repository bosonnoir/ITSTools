package fr.lip6.move.promela.togal.transform.representations;

import static fr.lip6.move.promela.togal.utils.GALUtils.*;
import static fr.lip6.move.promela.togal.utils.TransfoUtils.unsupported;
import static fr.lip6.move.promela.typing.PromelaTypeProvider.typeFor;

import java.util.ArrayList;
import java.util.List;

import fr.lip6.move.gal.Statement;
import fr.lip6.move.gal.ArrayPrefix;
import fr.lip6.move.gal.BooleanExpression;
import fr.lip6.move.gal.ComparisonOperators;
import fr.lip6.move.gal.GF2;
import fr.lip6.move.gal.GalFactory;
import fr.lip6.move.gal.Variable;
import fr.lip6.move.gal.VariableReference;
import fr.lip6.move.promela.promela.AtomicRef;
import fr.lip6.move.promela.promela.ChanVariable;
import fr.lip6.move.promela.promela.LiteralConstant;
import fr.lip6.move.promela.promela.Poll;
import fr.lip6.move.promela.promela.Receive;
import fr.lip6.move.promela.promela.ReceiveArg;
import fr.lip6.move.promela.promela.Send;
import fr.lip6.move.promela.togal.transform.Converter;
import fr.lip6.move.promela.typing.PChannelType;
import fr.lip6.move.promela.typing.PromelaType;

public class ChannelRepresentation {

	private ChanVariable pmlChan;
	private String name;
	private int size;
	private List<PromelaType> type;

	private ArrayPrefix gChanArray; // TODO: list when multiple type
	private Variable gChanAvail;

	private ChannelRepresentation(ChanVariable pmlChan, String name) {
		super();
		this.pmlChan = pmlChan;
		this.name = name;
		PChannelType pct = (PChannelType) typeFor(pmlChan);
		this.size = pct.getSize();

		this.type = pct.getKinds();
		if (type.size() != 1)
			throw unsupported("Multi-type channels currently unsupported.");

		// Create new VAR!!
		gChanAvail = GalFactory.eINSTANCE.createVariable();
		gChanAvail.setName(name + "__Channel_Avail");
		gChanAvail.setComment("/** counts items in channel " + name + " */");
		gChanAvail.setValue(GF2.constant(size));

		// FIXME: simple channel.
		gChanArray = makeArray(name + "__Channel", size, GF2.constant(0));
		gChanArray.setComment("/** channel " + name + " */");
	}

	public static ChannelRepresentation createRepresentation(
			ChanVariable pmlChan, String name) {
		return new ChannelRepresentation(pmlChan, name);
	}

	// TODO: tous les méthode de Poll, Send, (isGood?: pour rajouter garde)

	// //// Send

	public List<Statement> makeSend(Send s, Converter c) {
		boolean fifo = s.isFifo();
		if (!fifo)
			throw unsupported("Non FIFO channels currently unsupported. ");
		List<Statement> acs = new ArrayList<Statement>();
		// Need converter!!
		Statement as = GF2.createAssignment(
				makeArrayAccess(gChanArray, makeRef(gChanAvail)),
				c.convertInt(s.getArgs().getArgs().get(0)) // MAYBE RENAME
		);

		as.setComment("/** Send on channel */");
		Statement aau = makeAssignInc(makeRef(gChanAvail));
		aau.setComment("/** update available  */");
		acs.add(as);
		acs.add(aau);
		return acs;
	}

	public BooleanExpression makeSendGuard(Converter c) {
		// LATER: appel récursif sur expressuon
		// comparaison avec size
		return GF2.createComparison(makeRef(gChanAvail),ComparisonOperators.LT, GF2.constant(size));
	}

	// TODO: maybe, makeSendGuard !

	public List<Statement> makeReceive(Receive r, Converter c) {
		boolean random = !r.isNormal();
		boolean keep = r.isKeep();

		if (random)
			throw unsupported("Random Receive is not supported currently. ");

		List<Statement> acs = new ArrayList<Statement>();

		// int offset = random ? 2 : 1;
		// RANDOM MORE RAFINE.

		if (r.getArgs().getRecArgs().size() != 1)
			throw unsupported("Multi-parameter receive is not supported currently. ");

		ReceiveArg rarg = r.getArgs().getRecArgs().get(0);

		if (rarg instanceof AtomicRef) {

			// Retrieve variable et mise à jour
			Variable v = c.getEnv().getAtomic((AtomicRef) rarg);
			VariableReference vr = makeRef(v);
			Statement a = GF2.createAssignment(vr,
					makeArrayAccess(gChanArray, GF2.constant(0)));
			a.setComment("/** Receive from variable " + v.getName()
					+ " */");
			acs.add(a);
		}
		// sinon c'est un receive filter!

		// Consommation du message
		if (!keep) {
			// on réécrit.
			// ForImpl f = GalFactory.eINSTANCE.createFor();
			// Bourrin en attendant le for...

			// c.getEnv().getAtomic(null)
			for (int i = 0; i < size - 1; i++) {
				acs.add(GF2.createAssignment(makeArrayAccess(gChanArray, GF2.constant(i)),
						makeArrayAccess(gChanArray, GF2.constant(i + 1))));
			}

		}

		return acs;

	}

	public BooleanExpression makeReceiveGuard(Receive r, Converter converter) {
		// LATER improve avec filtres
		BooleanExpression availGuard = GF2.createComparison(makeRef(gChanAvail), ComparisonOperators.GT,
				GF2.constant(0));

		ReceiveArg rarg = r.getArgs().getRecArgs().get(0);
		if (rarg instanceof AtomicRef) // TODO see Macro !!
			return availGuard;
		else {
			if (rarg instanceof LiteralConstant) {
				LiteralConstant lc = (LiteralConstant) rarg;
				BooleanExpression filter =
				GF2.createComparison(makeArrayAccess(gChanArray, GF2.constant(0)), ComparisonOperators.EQ,
						GF2.constant(lc.getValue()));
				return GF2.and(availGuard,filter);
			}
		}
		throw unsupported("Something bad happened in receive");
	}

	public BooleanExpression makePoll(Poll p, Converter c) {
		
		// TODO ?
		//boolean random = !p.isNormal();
		
		// CHECK not an action
		// LATER improve avec filtres
		// (factorizer avec la garde du receive?)

		// ReceiveArg rarg = p.getArgs().getRecArgs().get(0);

		return GF2.createComparison(makeRef(gChanAvail), 
				ComparisonOperators.GT, GF2.constant(0));

	}

	// / Getters

	public ChanVariable getPmlChan() {
		return pmlChan;
	}

	public String getName() {
		return name;
	}

	public int getSize() {
		return size;
	}

	public List<PromelaType> getType() {
		return type;
	}

	public ArrayPrefix getgChanArray() {
		return gChanArray;
	}

	public Variable getgChanAvail() {
		return gChanAvail;
	}

}
