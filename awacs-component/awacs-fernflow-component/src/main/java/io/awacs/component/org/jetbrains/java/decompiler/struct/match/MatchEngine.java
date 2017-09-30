/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.awacs.component.org.jetbrains.java.decompiler.struct.match;

import io.awacs.component.org.jetbrains.java.decompiler.modules.decompiler.exps.ExitExprent;
import io.awacs.component.org.jetbrains.java.decompiler.modules.decompiler.exps.Exprent;
import io.awacs.component.org.jetbrains.java.decompiler.modules.decompiler.exps.FunctionExprent;
import io.awacs.component.org.jetbrains.java.decompiler.modules.decompiler.stats.IfStatement;
import io.awacs.component.org.jetbrains.java.decompiler.modules.decompiler.stats.Statement;
import io.awacs.component.org.jetbrains.java.decompiler.struct.gen.VarType;

import java.util.*;


public class MatchEngine {

  private MatchNode rootNode = null;
  
  private final Map<String, Object> variables = new HashMap<>();
  
  private static final Map<String, IMatchable.MatchProperties> stat_properties = new HashMap<>();
  private static final Map<String, IMatchable.MatchProperties> expr_properties = new HashMap<>();
  private static final Map<String, Integer> stat_type = new HashMap<>();
  private static final Map<String, Integer> expr_type = new HashMap<>();
  private static final Map<String, Integer> expr_func_type = new HashMap<>();
  private static final Map<String, Integer> expr_exit_type = new HashMap<>();
  private static final Map<String, Integer> stat_if_type = new HashMap<>();
  private static final Map<String, VarType> expr_const_type = new HashMap<>();
  
  static {
    stat_properties.put("type", IMatchable.MatchProperties.STATEMENT_TYPE);
    stat_properties.put("ret", IMatchable.MatchProperties.STATEMENT_RET);
    stat_properties.put("position", IMatchable.MatchProperties.STATEMENT_POSITION);
    stat_properties.put("statsize", IMatchable.MatchProperties.STATEMENT_STATSIZE);
    stat_properties.put("exprsize", IMatchable.MatchProperties.STATEMENT_EXPRSIZE);
    stat_properties.put("iftype", IMatchable.MatchProperties.STATEMENT_IFTYPE);
    
    expr_properties.put("type", IMatchable.MatchProperties.EXPRENT_TYPE);
    expr_properties.put("ret", IMatchable.MatchProperties.EXPRENT_RET);
    expr_properties.put("position", IMatchable.MatchProperties.EXPRENT_POSITION);
    expr_properties.put("functype", IMatchable.MatchProperties.EXPRENT_FUNCTYPE);
    expr_properties.put("exittype", IMatchable.MatchProperties.EXPRENT_EXITTYPE);
    expr_properties.put("consttype", IMatchable.MatchProperties.EXPRENT_CONSTTYPE);
    expr_properties.put("constvalue", IMatchable.MatchProperties.EXPRENT_CONSTVALUE);
    expr_properties.put("invclass", IMatchable.MatchProperties.EXPRENT_INVOCATION_CLASS);
    expr_properties.put("signature", IMatchable.MatchProperties.EXPRENT_INVOCATION_SIGNATURE);
    expr_properties.put("parameter", IMatchable.MatchProperties.EXPRENT_INVOCATION_PARAMETER);
    expr_properties.put("index", IMatchable.MatchProperties.EXPRENT_VAR_INDEX);
    expr_properties.put("name", IMatchable.MatchProperties.EXPRENT_FIELD_NAME);
    
    stat_type.put("if", Statement.TYPE_IF);
    stat_type.put("do", Statement.TYPE_DO);
    stat_type.put("switch", Statement.TYPE_SWITCH);
    stat_type.put("trycatch", Statement.TYPE_TRYCATCH);
    stat_type.put("basicblock", Statement.TYPE_BASICBLOCK);
    stat_type.put("sequence", Statement.TYPE_SEQUENCE);
    
    expr_type.put("array", Exprent.EXPRENT_ARRAY);
    expr_type.put("assignment", Exprent.EXPRENT_ASSIGNMENT);
    expr_type.put("constant", Exprent.EXPRENT_CONST);
    expr_type.put("exit", Exprent.EXPRENT_EXIT);
    expr_type.put("field", Exprent.EXPRENT_FIELD);
    expr_type.put("function", Exprent.EXPRENT_FUNCTION);
    expr_type.put("if", Exprent.EXPRENT_IF);
    expr_type.put("invocation", Exprent.EXPRENT_INVOCATION);
    expr_type.put("monitor", Exprent.EXPRENT_MONITOR);
    expr_type.put("new", Exprent.EXPRENT_NEW);
    expr_type.put("switch", Exprent.EXPRENT_SWITCH);
    expr_type.put("var", Exprent.EXPRENT_VAR);
    expr_type.put("annotation", Exprent.EXPRENT_ANNOTATION);
    expr_type.put("assert", Exprent.EXPRENT_ASSERT);
    
    expr_func_type.put("eq", FunctionExprent.FUNCTION_EQ);
    
    expr_exit_type.put("return", ExitExprent.EXIT_RETURN);
    expr_exit_type.put("throw", ExitExprent.EXIT_THROW);

    stat_if_type.put("if", IfStatement.IFTYPE_IF);
    stat_if_type.put("ifelse", IfStatement.IFTYPE_IFELSE);

    expr_const_type.put("null", VarType.VARTYPE_NULL);
    expr_const_type.put("string", VarType.VARTYPE_STRING);
  }
  

  public void parse(String description) {
    
    // each line is a separate statement/exprent 
    String[] lines = description.split("\n");

    int depth = 0; 
    LinkedList<MatchNode> stack = new LinkedList<>();
    
    for(String line : lines) {
      
      List<String> properties = new ArrayList<>(Arrays.asList(line.split("\\s+"))); // split on any number of whitespaces
      if(properties.get(0).isEmpty()) {
        properties.remove(0);
      }
      
      int node_type = "statement".equals(properties.get(0)) ? MatchNode.MATCHNODE_STATEMENT : MatchNode.MATCHNODE_EXPRENT;
      
      // create new node
      MatchNode matchNode = new MatchNode(node_type);
      for(int i = 1; i < properties.size(); ++i) {
        String[] values = properties.get(i).split(":");
        
        IMatchable.MatchProperties property = (node_type == MatchNode.MATCHNODE_STATEMENT ? stat_properties : expr_properties).get(values[0]);
        if(property == null) { // unknown property defined
          throw new RuntimeException("Unknown matching property");
        } else {
          
          Object value = null;
          int parameter = 0;
          
          String strValue = values[1];
          if(values.length == 3) {
            parameter = Integer.parseInt(values[1]);
            strValue = values[2];
          }
          
          switch(property) {
          case STATEMENT_TYPE:
            value = stat_type.get(strValue);
            break;
          case STATEMENT_STATSIZE:
          case STATEMENT_EXPRSIZE:
            value = Integer.valueOf(strValue);
            break;
          case STATEMENT_POSITION:
          case EXPRENT_POSITION:
          case EXPRENT_INVOCATION_CLASS:
          case EXPRENT_INVOCATION_SIGNATURE:
          case EXPRENT_INVOCATION_PARAMETER:
          case EXPRENT_VAR_INDEX:
          case EXPRENT_FIELD_NAME:
          case EXPRENT_CONSTVALUE:
          case STATEMENT_RET:
          case EXPRENT_RET:
            value = strValue;
            break;
          case STATEMENT_IFTYPE:
            value = stat_if_type.get(strValue);
            break;
          case EXPRENT_FUNCTYPE:
            value = expr_func_type.get(strValue);
            break;
          case EXPRENT_EXITTYPE:
            value = expr_exit_type.get(strValue);
            break;
          case EXPRENT_CONSTTYPE:
            value = expr_const_type.get(strValue);
            break;
          case EXPRENT_TYPE:
            value = expr_type.get(strValue);
            break;
          default:
            throw new RuntimeException("Unhandled matching property");
          }

          matchNode.addRule(property, new MatchNode.RuleValue(parameter, value));
        }
      }
      
      if(stack.isEmpty()) { // first line, root node
        stack.push(matchNode);
      } else {
      
        // return to the correct parent on the stack  
        int new_depth = line.lastIndexOf(' ', depth) + 1;
        for(int i = new_depth; i <= depth; ++i) {
          stack.pop();
        }
  
        // insert new node
        stack.getFirst().addChild(matchNode);
        stack.push(matchNode);
        
        depth = new_depth; 
      }
    }
    
    this.rootNode = stack.getLast();
  }
  
  public boolean match(IMatchable object) {
    variables.clear();
    return match(this.rootNode, object);
  }
  
  private boolean match(MatchNode matchNode, IMatchable object) {
    
    if(!object.match(matchNode, this)) { 
      return false;
    }
    
    int expr_index = 0;
    int stat_index = 0;

    for(MatchNode childNode : matchNode.getChildren()) {
      boolean isStatement = childNode.getType() == MatchNode.MATCHNODE_STATEMENT;
      
      IMatchable childObject = object.findObject(childNode, isStatement ? stat_index : expr_index);
      if(childObject == null || !match(childNode, childObject)) {
        return false;
      }
      
      if(isStatement) {
        stat_index++;
      } else {
        expr_index++;
      }
    }
    
    return true;
  }

  public boolean checkAndSetVariableValue(String name, Object value) {
    
    Object old_value = variables.get(name);
    if(old_value == null) {
      variables.put(name, value);
    } else if(!old_value.equals(value)) {
      return false;
    }
    
    return true;
  }
  
  public Object getVariableValue(String name) {
    return variables.get(name);
  }
  
}
