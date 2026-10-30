/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.learn;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.StringUtil;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ext.admonition.AdmonitionExtension;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.aside.AsideExtension;
import com.vladsch.flexmark.ext.attributes.AttributesExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.media.tags.MediaTagsExtension;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterBlock;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterNode;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterVisitor;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterVisitorExt;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeVisitor;
import com.vladsch.flexmark.util.ast.TextCollectingVisitor;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.io.File;

import java.nio.charset.StandardCharsets;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import org.yaml.snakeyaml.Yaml;

/**
 * @author Francesco Acciaro
 */
public class MarkdownParser {

	public MarkdownParser() {
		MutableDataSet mutableDataSet = new MutableDataSet(
		).set(
			AdmonitionExtension.QUALIFIER_TYPE_MAP,
			HashMapBuilder.put(
				"error", "error"
			).put(
				"important", "important"
			).put(
				"note", "note"
			).put(
				"tip", "tip"
			).put(
				"warning", "warning"
			).build()
		).set(
			AdmonitionExtension.TYPE_SVG_MAP, new HashMap<String, String>()
		).set(
			AsideExtension.ALLOW_LEADING_SPACE, true
		).set(
			AsideExtension.EXTEND_TO_BLANK_LINE, false
		).set(
			AsideExtension.IGNORE_BLANK_LINE, false
		).set(
			AsideExtension.INTERRUPTS_ITEM_PARAGRAPH, true
		).set(
			AsideExtension.INTERRUPTS_PARAGRAPH, true
		).set(
			AsideExtension.WITH_LEAD_SPACES_INTERRUPTS_ITEM_PARAGRAPH, true
		).set(
			HtmlRenderer.GENERATE_HEADER_ID, true
		).set(
			Parser.EXTENSIONS,
			Arrays.asList(
				AdmonitionExtension.create(), AnchorLinkExtension.create(),
				AsideExtension.create(), AttributesExtension.create(),
				DefinitionExtension.create(), FootnoteExtension.create(),
				MediaTagsExtension.create(), StrikethroughExtension.create(),
				SuperscriptExtension.create(), TablesExtension.create(),
				TocExtension.create(), TypographicExtension.create(),
				YamlFrontMatterExtension.create())
		);

		_parser = Parser.builder(
			mutableDataSet
		).build();
	}

	public String getDescription(String text) {
		TextCollectingVisitor textCollectingVisitor =
			new TextCollectingVisitor();

		return StringUtil.shorten(
			textCollectingVisitor.collectAndGetText(_parser.parse(text)), 300);
	}

	public Map<String, Object> getFrontMatter(File file) throws Exception {
		return getFrontMatter(
			FileUtils.readFileToString(file, StandardCharsets.UTF_8));
	}

	public Map<String, Object> getFrontMatter(String text) {
		SnakeYamlFrontMatterVisitor snakeYamlFrontMatterVisitor =
			new SnakeYamlFrontMatterVisitor();

		snakeYamlFrontMatterVisitor.visit(_parser.parse(text));

		return snakeYamlFrontMatterVisitor.getData();
	}

	public String getTitle(String text) {
		return _getTitle(_parser.parse(text));
	}

	public String getUuid(Map<String, Object> data) {
		if ((data == null) || !data.containsKey("uuid")) {
			return StringPool.BLANK;
		}

		Object uuid = data.get("uuid");

		if (!(uuid instanceof String)) {
			return StringPool.BLANK;
		}

		return uuid.toString();
	}

	public String getUuid(String text) {
		return getUuid(getFrontMatter(text));
	}

	public boolean isShowChildrenCards(File file) throws Exception {
		Map<String, Object> data = getFrontMatter(file);

		if ((data == null) || !data.containsKey("showChildrenCards") ||
			!StringUtil.equals(
				data.get(
					"showChildrenCards"
				).toString(),
				"false")) {

			return true;
		}

		return GetterUtil.getBoolean(data.get("showChildrenCards"));
	}

	private String _getTitle(Node node) {
		if (node instanceof Heading) {
			Heading heading = (Heading)node;

			if ((heading.getLevel() == 1) && heading.hasChildren()) {
				TextCollectingVisitor textCollectingVisitor =
					new TextCollectingVisitor();

				return textCollectingVisitor.collectAndGetText(heading);
			}
		}

		if ((node instanceof Block) && node.hasChildren()) {
			Node childNode = node.getFirstChild();

			while (childNode != null) {
				String title = _getTitle(childNode);

				if (title != null) {
					return title;
				}

				childNode = childNode.getNext();
			}
		}

		return null;
	}

	private final Parser _parser;
	private final Yaml _yaml = new Yaml();

	private class SnakeYamlFrontMatterVisitor
		implements YamlFrontMatterVisitor {

		public Map<String, Object> getData() {
			return _data;
		}

		public void visit(Node node) {
			_yamlFrontMatterVisitor.visit(node);
		}

		@Override
		public void visit(YamlFrontMatterBlock yamlFrontMatterBlock) {
			String yamlString = String.valueOf(yamlFrontMatterBlock.getChars());

			yamlString = yamlString.replaceAll("---", "");

			_data = _yaml.load(yamlString);
		}

		@Override
		public void visit(YamlFrontMatterNode yamlFrontMatterNode) {
		}

		private Map<String, Object> _data;
		private final NodeVisitor _yamlFrontMatterVisitor = new NodeVisitor(
			YamlFrontMatterVisitorExt.VISIT_HANDLERS(this));

	}

}