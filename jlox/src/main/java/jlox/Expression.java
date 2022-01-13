package jlox;

import java.util.List;

abstract class Expression {
	interface Visitor<R> {
		R visitAssignExpression(Assign expression);
		R visitBinaryExpression(Binary expression);
		R visitCallExpression(Call expression);
		R visitGetExpression(Get expression);
		R visitGroupingExpression(Grouping expression);
		R visitLiteralExpression(Literal expression);
		R visitLogicalExpression(Logical expression);
		R visitSetExpression(Set expression);
		R visitUnaryExpression(Unary expression);
		R visitVariableExpression(Variable expression);
	}
	static class Assign extends Expression {
		final Token name;
		final Expression value;

		Assign(Token name,Expression value) {
			this.name = name;
			this.value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitAssignExpression(this);
		}
	}

	static class Binary extends Expression {
		final Expression left;
		final Token operator;
		final Expression right;

		Binary(Expression left,Token operator,Expression right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitBinaryExpression(this);
		}
	}

	static class Call extends Expression {
		final Expression callee;
		final Token paren;
		final List<Expression> arguments;

		Call(Expression callee,Token paren,List<Expression> arguments) {
			this.callee = callee;
			this.paren = paren;
			this.arguments = arguments;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitCallExpression(this);
		}
	}

	static class Get extends Expression {
		final Expression object;
		final Token name;

		Get(Expression object,Token name) {
			this.object = object;
			this.name = name;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitGetExpression(this);
		}
	}

	static class Grouping extends Expression {
		final Expression expression;

		Grouping(Expression expression) {
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitGroupingExpression(this);
		}
	}

	static class Literal extends Expression {
		final Object value;

		Literal(Object value) {
			this.value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLiteralExpression(this);
		}
	}

	static class Logical extends Expression {
		final Expression left;
		final Token operator;
		final Expression right;

		Logical(Expression left,Token operator,Expression right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitLogicalExpression(this);
		}
	}

	static class Set extends Expression {
		final Expression object;
		final Token name;
		final Expression value;

		Set(Expression object,Token name,Expression value) {
			this.object = object;
			this.name = name;
			this.value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitSetExpression(this);
		}
	}

	static class Unary extends Expression {
		final Token operator;
		final Expression expression;

		Unary(Token operator,Expression expression) {
			this.operator = operator;
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitUnaryExpression(this);
		}
	}

	static class Variable extends Expression {
		final Token name;

		Variable(Token name) {
			this.name = name;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			return visitor.visitVariableExpression(this);
		}
	}


	abstract <R> R accept(Visitor<R> visitor);
}
