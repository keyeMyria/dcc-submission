package org.icgc.dcc.validation.restriction;

import org.icgc.dcc.dictionary.model.Field;
import org.icgc.dcc.dictionary.model.Restriction;
import org.icgc.dcc.validation.FlowType;
import org.icgc.dcc.validation.InternalPlanElement;
import org.icgc.dcc.validation.PlanElement;
import org.icgc.dcc.validation.RestrictionType;
import org.icgc.dcc.validation.RestrictionTypeSchema;
import org.icgc.dcc.validation.ValidationErrorCode;
import org.icgc.dcc.validation.cascading.ValidationFields;

import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Function;
import cascading.operation.FunctionCall;
import cascading.pipe.Each;
import cascading.pipe.Pipe;
import cascading.tuple.Fields;
import cascading.tuple.TupleEntry;

public class RequiredRestriction implements InternalPlanElement {

  public static final String NAME = "required";// TODO: create enum for valid Restriction types?

  private final String field;

  private final boolean acceptMissingCode;

  protected RequiredRestriction(String field, boolean acceptMissingCode) {
    this.field = field;
    this.acceptMissingCode = acceptMissingCode;
  }

  @Override
  public String describe() {
    return String.format("%s[%s]", NAME, field);
  }

  @Override
  public Pipe extend(Pipe pipe) {
    return new Each(pipe, new ValidationFields(field), new SpecifiedFunction(this.isAcceptMissingCode()),
        Fields.REPLACE);
  }

  public static class Type implements RestrictionType {

    private final RestrictionTypeSchema schema = new RestrictionTypeSchema();

    @Override
    public String getType() {
      return NAME;
    }

    @Override
    public FlowType flow() {
      return FlowType.INTERNAL;
    }

    @Override
    public boolean builds(String name) {
      return getType().equals(name);
    }

    @Override
    public RestrictionTypeSchema getSchema() {
      return schema;
    }

    @Override
    public PlanElement build(Field field, Restriction restriction) {
      if(restriction.getConfig() == null || restriction.getConfig().get("acceptMissingCode") == null) {
        return new RequiredRestriction(field.getName(), true);
      }
      Object acceptMissingCode = restriction.getConfig().get("acceptMissingCode");
      return new RequiredRestriction(field.getName(), Boolean.parseBoolean((String) acceptMissingCode));

    }

  }

  @SuppressWarnings("rawtypes")
  public static class SpecifiedFunction extends BaseOperation implements Function {
    private final boolean acceptMissingCode;

    protected SpecifiedFunction(boolean acceptMissingCode) {
      super(2, Fields.ARGS);
      this.acceptMissingCode = acceptMissingCode;
    }

    @Override
    public void operate(FlowProcess flowProcess, FunctionCall functionCall) {
      TupleEntry tupleEntry = functionCall.getArguments();
      String value = tupleEntry.getString(0);

      if(ValidationFields.state(tupleEntry).isFieldMissing((String) tupleEntry.getFields().get(0)) == false
          && (value == null || value.isEmpty())) {
        Object fieldName = tupleEntry.getFields().get(0);
        ValidationFields.state(tupleEntry).reportError(ValidationErrorCode.MISSING_VALUE_ERROR, value, fieldName);
      } else if(ValidationFields.state(tupleEntry).isFieldMissing((String) tupleEntry.getFields().get(0))
          && !acceptMissingCode) {
        Object fieldName = tupleEntry.getFields().get(0);
        ValidationFields.state(tupleEntry).reportError(ValidationErrorCode.MISSING_VALUE_ERROR, value, fieldName);
      }
      functionCall.getOutputCollector().add(tupleEntry.getTupleCopy());
    }

  }

  private boolean isAcceptMissingCode() {
    return acceptMissingCode;
  }
}
