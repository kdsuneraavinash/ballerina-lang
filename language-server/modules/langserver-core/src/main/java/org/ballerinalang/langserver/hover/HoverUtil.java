/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.hover;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.Documentable;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.langserver.common.constants.ContextConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.HoverContext;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class for Hover functionality of language server.
 */
public class HoverUtil {

    /**
     * Get the hover content.
     *
     * @param context Hover operation context
     * @return {@link Hover} Hover content
     */
    public static Hover getHover(HoverContext context) {
        Optional<Document> srcFile = context.currentDocument();
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();
        if (semanticModel.isEmpty() || srcFile.isEmpty()) {
            return HoverUtil.getDefaultHoverObject();
        }

        Position cursorPosition = context.getCursorPosition();
        LinePosition linePosition = LinePosition.from(cursorPosition.getLine(), cursorPosition.getCharacter());
        Optional<Symbol> symbolAtCursor = semanticModel.get().symbol(srcFile.get(), linePosition);
        if (symbolAtCursor.isEmpty()) {
            return HoverUtil.getDefaultHoverObject();
        }

        switch (symbolAtCursor.get().kind()) {
            case FUNCTION:
                return getFunctionHoverMarkupContent((FunctionSymbol) symbolAtCursor.get(), context);
            case METHOD:
                return getFunctionHoverMarkupContent((MethodSymbol) symbolAtCursor.get(), context);
            case RESOURCE_METHOD:
                return getFunctionHoverMarkupContent((ResourceMethodSymbol) symbolAtCursor.get(), context);
            case TYPE_DEFINITION:
                return getTypeDefHoverMarkupContent((TypeDefinitionSymbol) symbolAtCursor.get(), context);
            case CLASS:
                return getClassHoverMarkupContent((ClassSymbol) symbolAtCursor.get(), context);
            case CONSTANT:
            case ANNOTATION:
            case ENUM:
            case VARIABLE:
                return getDescriptionOnlyHoverObject(symbolAtCursor.get());
            case TYPE:
                if (((TypeSymbol) symbolAtCursor.get()).typeKind() == TypeDescKind.TYPE_REFERENCE) {
                    return getTypeRefHoverMarkupContent((TypeReferenceTypeSymbol) symbolAtCursor.get(),
                            semanticModel.get(), srcFile.get(), context);
                }
                return getDefaultHoverObject();
            default:
                return HoverUtil.getDefaultHoverObject();
        }
    }

    private static Hover getObjectHoverMarkupContent(Documentation documentation, ObjectTypeSymbol classSymbol,
                                                     HoverContext context) {
        List<String> hoverContent = new ArrayList<>();
        if (documentation.description().isPresent()) {
            hoverContent.add(documentation.description().get());
        }

        Map<String, String> paramsMap = documentation.parameterMap();
        if (!paramsMap.isEmpty()) {
            List<String> params = new ArrayList<>();
            params.add(header(3, ContextConstants.FIELD_TITLE) + CommonUtil.MD_LINE_SEPARATOR);

            params.addAll(classSymbol.fieldDescriptors().entrySet().stream()
                    .map(fieldEntry -> {
                        String desc = paramsMap.get(fieldEntry.getKey());
                        String modifiedTypeName =
                                CommonUtil.getModifiedTypeName(context, fieldEntry.getValue().typeDescriptor());
                        return quotedString(modifiedTypeName) + " " + italicString(boldString(fieldEntry.getKey()))
                                + " : " + desc;
                    }).collect(Collectors.toList()));

            hoverContent.add(String.join(CommonUtil.MD_LINE_SEPARATOR, params));
        }

        List<String> methods = new ArrayList<>();
        classSymbol.methods().forEach((name, method) -> {
            StringBuilder methodInfo = new StringBuilder();
            Optional<Documentation> methodDoc = method.documentation();
            methodInfo.append(quotedString(CommonUtil.getModifiedTypeName(context, method.typeDescriptor())));
            if (methodDoc.isPresent() && methodDoc.get().description().isPresent()) {
                methodInfo.append(CommonUtil.MD_LINE_SEPARATOR).append(methodDoc.get().description().get());
            }
            methods.add(bulletItem(methodInfo.toString()));
        });

        if (!methods.isEmpty()) {
            methods.add(0, header(3, ContextConstants.METHOD_TITLE) + CommonUtil.MD_LINE_SEPARATOR);
            hoverContent.add(String.join(CommonUtil.MD_LINE_SEPARATOR, methods));
        }

        Hover hover = new Hover();
        MarkupContent hoverMarkupContent = new MarkupContent();
        hoverMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        hoverMarkupContent.setValue(hoverContent.stream().collect(Collectors.joining(getHorizontalSeparator())));
        hover.setContents(hoverMarkupContent);

        return hover;
    }

    private static Hover getTypeDefHoverMarkupContent(TypeDefinitionSymbol symbol, HoverContext context) {
        TypeSymbol rawType = CommonUtil.getRawType(symbol.typeDescriptor());
        Optional<Documentation> documentation = symbol.documentation();

        if (documentation.isEmpty()) {
            return getDefaultHoverObject();
        }

        if (rawType.typeKind() == TypeDescKind.RECORD) {
            return getRecordTypeHoverContent(documentation.get(), (RecordTypeSymbol) rawType, context);
        }
        if (rawType.typeKind() == TypeDescKind.OBJECT) {
            return getObjectHoverMarkupContent(documentation.get(), (ObjectTypeSymbol) rawType, context);
        }

        return getDescriptionOnlyHoverObject(documentation.get());
    }

    private static Hover getClassHoverMarkupContent(ClassSymbol symbol, HoverContext context) {
        Optional<Documentation> documentation = symbol.documentation();

        if (documentation.isEmpty()) {
            return getDefaultHoverObject();
        }

        return getObjectHoverMarkupContent(documentation.get(), symbol, context);
    }

    private static Hover getRecordTypeHoverContent(Documentation documentation,
                                                   RecordTypeSymbol recordType,
                                                   HoverContext ctx) {
        List<String> hoverContent = new ArrayList<>();
        if (documentation.description().isPresent()) {
            hoverContent.add(documentation.description().get());
        }

        Map<String, String> paramsMap = documentation.parameterMap();
        if (!paramsMap.isEmpty()) {
            List<String> params = new ArrayList<>();
            params.add(header(3, ContextConstants.FIELD_TITLE) + CommonUtil.MD_LINE_SEPARATOR);

            params.addAll(recordType.fieldDescriptors().entrySet().stream()
                    .map(fieldEntry -> {
                        String desc = paramsMap.get(fieldEntry.getKey());
                        String typeName = CommonUtil.getModifiedTypeName(ctx, fieldEntry.getValue().typeDescriptor());
                        return quotedString(typeName) + " " + italicString(boldString(fieldEntry.getKey()))
                                + " : " + desc;
                    }).collect(Collectors.toList()));
            Optional<TypeSymbol> restTypeDesc = recordType.restTypeDescriptor();
            restTypeDesc.ifPresent(typeSymbol ->
                    params.add(quotedString(CommonUtil.getModifiedTypeName(ctx, typeSymbol) + "...")));
            hoverContent.add(String.join(CommonUtil.MD_LINE_SEPARATOR, params));
        }

        Hover hover = new Hover();
        MarkupContent hoverMarkupContent = new MarkupContent();
        hoverMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        hoverMarkupContent.setValue(hoverContent.stream().collect(Collectors.joining(getHorizontalSeparator())));
        hover.setContents(hoverMarkupContent);

        return hover;
    }

    private static Hover getTypeRefHoverMarkupContent(TypeReferenceTypeSymbol typeSymbol, SemanticModel model,
                                                      Document srcFile, HoverContext context) {
        Optional<Symbol> associatedDef = model.symbol(srcFile, typeSymbol.getLocation().get().lineRange().startLine());

        if (associatedDef.isEmpty()) {
            return getDefaultHoverObject();
        }

        switch (associatedDef.get().kind()) {
            case TYPE_DEFINITION:
                return getTypeDefHoverMarkupContent((TypeDefinitionSymbol) associatedDef.get(), context);
            case ENUM:
                return getDescriptionOnlyHoverObject(associatedDef.get());
            case CLASS:
                return getClassHoverMarkupContent((ClassSymbol) associatedDef.get(), context);
        }

        return getDefaultHoverObject();
    }

    /**
     * Get the default hover object.
     *
     * @return {@link Hover} hover default hover object.
     */
    public static Hover getDefaultHoverObject() {
        Hover hover = new Hover();
        MarkupContent hoverMarkupContent = new MarkupContent();
        hoverMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        hoverMarkupContent.setValue("");
        hover.setContents(hoverMarkupContent);

        return hover;
    }

    /**
     * Get the description only hover object.
     *
     * @return {@link Hover}
     */
    private static Hover getDescriptionOnlyHoverObject(Documentation documentation) {
        String description = "";
        if (documentation.description().isPresent()) {
            description = documentation.description().get();
        }
        Hover hover = new Hover();
        MarkupContent hoverMarkupContent = new MarkupContent();
        hoverMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        hoverMarkupContent.setValue(description);
        hover.setContents(hoverMarkupContent);

        return hover;
    }

    /**
     * Get the description only hover object.
     *
     * @return {@link Hover}
     */
    public static Hover getDescriptionOnlyHoverObject(Symbol symbol) {
        if (!(symbol instanceof Documentable) || ((Documentable) symbol).documentation().isEmpty()) {
            return getDefaultHoverObject();
        }

        return getDescriptionOnlyHoverObject(((Documentable) symbol).documentation().get());
    }

//    private static boolean skipFirstParam(LSContext context, BInvokableSymbol invokableSymbol) {
    // Fixme in the next phase
//        NonTerminalNode evalNode = context.get(CompletionKeys.TOKEN_AT_CURSOR_KEY).parent();
//        return CommonUtil.isLangLibSymbol(invokableSymbol) && evalNode.kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE;
//        return false;
//    }

    private static Hover getFunctionHoverMarkupContent(FunctionSymbol symbol, HoverContext ctx) {
        Optional<Documentation> documentation = symbol.documentation();
        if (documentation.isEmpty()) {
            return getDefaultHoverObject();
        }

        List<String> hoverContent = new ArrayList<>();
        if (documentation.get().description().isPresent()) {
            hoverContent.add(documentation.get().description().get());
        }
        Map<String, String> paramsMap = documentation.get().parameterMap();
        if (!paramsMap.isEmpty()) {
            List<String> params = new ArrayList<>();
            params.add(header(3, ContextConstants.PARAM_TITLE) + CommonUtil.MD_LINE_SEPARATOR);

            params.addAll(symbol.typeDescriptor().parameters().stream()
                    .map(param -> {
                        if (param.getName().isEmpty()) {
                            return quotedString(CommonUtil.getModifiedTypeName(ctx, param.typeDescriptor()));
                        }
                        String paramName = param.getName().get();
                        String desc = paramsMap.get(paramName);
                        return quotedString(CommonUtil.getModifiedTypeName(ctx, param.typeDescriptor())) + " "
                                + italicString(boldString(paramName)) + " : " + desc;
                    }).collect(Collectors.toList()));

            Optional<ParameterSymbol> restParam = symbol.typeDescriptor().restParam();
            if (restParam.isPresent()) {
                String modifiedTypeName = CommonUtil.getModifiedTypeName(ctx, restParam.get().typeDescriptor());
                StringBuilder restParamBuilder = new StringBuilder(quotedString(modifiedTypeName + "..."));
                if (restParam.get().getName().isPresent()) {
                    restParamBuilder.append(" ").append(italicString(boldString(restParam.get().getName().get())))
                            .append(" : ").append(paramsMap.get(restParam.get().getName().get()));
                }
                params.add(restParamBuilder.toString());
            }

            hoverContent.add(String.join(CommonUtil.MD_LINE_SEPARATOR, params));
        }
        if (documentation.get().returnDescription().isPresent()) {
            String returnDoc = header(3, ContextConstants.RETURN_TITLE) + CommonUtil.MD_LINE_SEPARATOR +
                    CommonUtil.getModifiedTypeName(ctx, symbol.typeDescriptor().returnTypeDescriptor().orElseThrow())
                    + documentation.get().returnDescription().get();
            hoverContent.add(returnDoc);
        }

        Hover hover = new Hover();
        MarkupContent hoverMarkupContent = new MarkupContent();
        hoverMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        hoverMarkupContent.setValue(hoverContent.stream().collect(Collectors.joining(getHorizontalSeparator())));
        hover.setContents(hoverMarkupContent);

        return hover;
    }

    private static String getHorizontalSeparator() {
        return CommonUtil.MD_LINE_SEPARATOR + CommonUtil.MD_LINE_SEPARATOR + "---"
                + CommonUtil.MD_LINE_SEPARATOR + CommonUtil.MD_LINE_SEPARATOR;
    }

    private static String quotedString(String value) {
        return "`" + value.trim() + "`";
    }

    private static String boldString(String value) {
        return "**" + value.trim() + "**";
    }

    private static String italicString(String value) {
        return "*" + value.trim() + "*";
    }

    private static String bulletItem(String value) {
        return "+ " + value.trim() + CommonUtil.MD_LINE_SEPARATOR;
    }

    private static String header(int level, String header) {
        return String.join("", Collections.nCopies(level, "#")) + " " + header;
    }
}
