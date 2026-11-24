/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.admin.address.internal.odata.filter.expression;

import com.liferay.petra.sql.dsl.Column;
import com.liferay.petra.sql.dsl.DSLFunctionFactoryUtil;
import com.liferay.petra.sql.dsl.Table;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.odata.entity.EntityField;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.odata.filter.expression.BinaryExpression;
import com.liferay.portal.odata.filter.expression.Expression;
import com.liferay.portal.odata.filter.expression.ExpressionVisitException;
import com.liferay.portal.odata.filter.expression.ExpressionVisitor;
import com.liferay.portal.odata.filter.expression.ListExpression;
import com.liferay.portal.odata.filter.expression.LiteralExpression;
import com.liferay.portal.odata.filter.expression.MemberExpression;
import com.liferay.portal.odata.filter.expression.MethodExpression;
import com.liferay.portal.odata.filter.expression.PrimitivePropertyExpression;
import com.liferay.portal.odata.filter.expression.UnaryExpression;

import java.time.Instant;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Balazs Breier
 */
public class PredicateExpressionVisitorImpl
	implements ExpressionVisitor<Object> {

	public PredicateExpressionVisitorImpl(
		EntityModel entityModel, Table<?> table) {

		_entityModel = entityModel;
		_table = table;
	}

	@Override
	public Object visitBinaryExpressionOperation(
			BinaryExpression.Operation operation, Object left, Object right)
		throws ExpressionVisitException {

		if (Objects.equals(BinaryExpression.Operation.AND, operation) ||
			Objects.equals(BinaryExpression.Operation.OR, operation)) {

			Predicate leftPredicate = Predicate.withParentheses(
				(Predicate)left);
			Predicate rightPredicate = Predicate.withParentheses(
				(Predicate)right);

			if (Objects.equals(BinaryExpression.Operation.AND, operation)) {
				return leftPredicate.and(rightPredicate);
			}

			return leftPredicate.or(rightPredicate);
		}

		Column<?, Object> column = (Column<?, Object>)left;

		Object value = _toValue(column, right);

		if (Objects.equals(BinaryExpression.Operation.EQ, operation)) {
			return column.eq(value);
		}
		else if (Objects.equals(BinaryExpression.Operation.GE, operation)) {
			return column.gte(value);
		}
		else if (Objects.equals(BinaryExpression.Operation.GT, operation)) {
			return column.gt(value);
		}
		else if (Objects.equals(BinaryExpression.Operation.LE, operation)) {
			return column.lte(value);
		}
		else if (Objects.equals(BinaryExpression.Operation.LT, operation)) {
			return column.lt(value);
		}
		else if (Objects.equals(BinaryExpression.Operation.NE, operation)) {
			return column.neq(value);
		}

		throw new ExpressionVisitException(
			"Unsupported binary operation " + operation);
	}

	@Override
	public Object visitListExpressionOperation(
			ListExpression.Operation operation, Object left,
			List<Object> rights)
		throws ExpressionVisitException {

		if (!Objects.equals(ListExpression.Operation.IN, operation)) {
			throw new ExpressionVisitException(
				"Unsupported list operation " + operation);
		}

		Column<?, Object> column = (Column<?, Object>)left;

		List<Object> values = new ArrayList<>();

		for (Object right : rights) {
			values.add(_toValue(column, right));
		}

		return column.in(values.toArray());
	}

	@Override
	public Object visitLiteralExpression(LiteralExpression literalExpression)
		throws ExpressionVisitException {

		String text = literalExpression.getText();

		if (Objects.equals(
				LiteralExpression.Type.DATE_TIME,
				literalExpression.getType())) {

			return Date.from(Instant.parse(text));
		}
		else if (Objects.equals(
					LiteralExpression.Type.DOUBLE,
					literalExpression.getType())) {

			return GetterUtil.getDouble(text);
		}
		else if (Objects.equals(
					LiteralExpression.Type.INTEGER,
					literalExpression.getType())) {

			return GetterUtil.getLong(text);
		}
		else if (Objects.equals(
					LiteralExpression.Type.STRING,
					literalExpression.getType())) {

			return StringUtil.replace(
				StringUtil.unquote(text), StringPool.DOUBLE_APOSTROPHE,
				StringPool.APOSTROPHE);
		}

		throw new ExpressionVisitException(
			"Unsupported literal type " + literalExpression.getType());
	}

	@Override
	public Object visitMemberExpression(MemberExpression memberExpression)
		throws ExpressionVisitException {

		Expression expression = memberExpression.getExpression();

		return expression.accept(this);
	}

	@Override
	public Object visitMethodExpression(
			List<Object> expressions, MethodExpression.Type type)
		throws ExpressionVisitException {

		if (!Objects.equals(MethodExpression.Type.CONTAINS, type) &&
			!Objects.equals(MethodExpression.Type.STARTS_WITH, type)) {

			throw new ExpressionVisitException("Unsupported method " + type);
		}

		Column<?, ?> column = (Column<?, ?>)expressions.get(0);
		Object value = expressions.get(1);

		if (!(value instanceof String) ||
			!Objects.equals(column.getJavaType(), String.class)) {

			throw new ExpressionVisitException(
				StringBundler.concat(
					"Unsupported method ", type, " for ", column.getName()));
		}

		String pattern = StringUtil.toLowerCase((String)value);

		if (Objects.equals(MethodExpression.Type.CONTAINS, type)) {
			pattern = StringPool.PERCENT + pattern;
		}

		return DSLFunctionFactoryUtil.lower(
			(Column<?, String>)column
		).like(
			pattern + StringPool.PERCENT
		);
	}

	@Override
	public Object visitPrimitivePropertyExpression(
		PrimitivePropertyExpression primitivePropertyExpression) {

		Map<String, EntityField> entityFieldsMap =
			_entityModel.getEntityFieldsMap();

		EntityField entityField = entityFieldsMap.get(
			primitivePropertyExpression.getName());

		return _table.getColumn(entityField.getFilterableName(null));
	}

	@Override
	public Object visitUnaryExpressionOperation(
			UnaryExpression.Operation operation, Object operand)
		throws ExpressionVisitException {

		if (Objects.equals(UnaryExpression.Operation.NOT, operation)) {
			return Predicate.not((Predicate)operand);
		}

		throw new ExpressionVisitException(
			"Unsupported unary operation " + operation);
	}

	private Object _toValue(Column<?, Object> column, Object value)
		throws ExpressionVisitException {

		Class<?> clazz = column.getJavaType();

		if (Objects.equals(clazz, Double.class) && (value instanceof Long)) {
			return Double.valueOf((Long)value);
		}

		if (!clazz.isInstance(value)) {
			throw new ExpressionVisitException(
				StringBundler.concat(
					"Invalid value ", value, " for ", column.getName()));
		}

		return value;
	}

	private final EntityModel _entityModel;
	private final Table<?> _table;

}