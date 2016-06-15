package net.sandius.rembulan.parser.ast;

public class VarargsExpr extends Expr {

	public VarargsExpr(SourceInfo src, Attributes attr) {
		super(src, attr);
	}

	@Override
	public Expr accept(Transformer tf) {
		return tf.transform(this);
	}

}
