/*
 * Copyright (c) 2012 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2012 [name of copyright owner]
 */

package com.evolveum.midpoint.web.component.orgStruct;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.LabeledWebMarkupContainer;
import org.apache.wicket.model.IModel;

import wickettree.AbstractTree;
import wickettree.AbstractTree.State;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.query.OrgFilter;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.web.component.button.AjaxLinkButton;
import com.evolveum.midpoint.web.page.admin.users.dto.OrgStructDto;
import com.evolveum.midpoint.xml.ns._public.common.common_2.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_2.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_2.OrgType;
import com.evolveum.midpoint.xml.ns._public.common.common_2.UserType;

/**
 * @author mserbak
 */
public class BookmarkableFolderContent extends Content {
	private static final String DOT_CLASS = BookmarkableFolderContent.class.getName() + ".";
	private static final String OPERATION_LOAD_ORGUNIT = DOT_CLASS + "load org unit";

	public BookmarkableFolderContent() {
	}

	@Override
	public Component newContentComponent(String id, final AbstractTree<NodeDto> tree,
			final IModel<NodeDto> model) {
		return new Node<NodeDto>(id, model) {

			@Override
			protected MarkupContainer newLinkComponent(String id, IModel<NodeDto> model) {
				final NodeDto node = model.getObject();
				if (tree.getProvider().hasChildren(node)) {
					return super.newLinkComponent(id, model);

				} else {
					return new LabeledWebMarkupContainer(id, model) {
					};
				}
			}

			@Override
			protected void onClick(AjaxRequestTarget target) {
				NodeDto t = getModelObject();
				if (tree.getState(t) == State.EXPANDED) {
					tree.collapse(t);
				} else {
					t.setNodes(getNodes(t));
					tree.expand(t);
				}
				target.appendJavaScript("initMenuButtons()");
			}

			@Override
			protected String getStyleClass() {
				NodeDto t = getModelObject();
				String styleClass;
				if (tree.getProvider().hasChildren(t)) {
					if (tree.getState(t) == State.EXPANDED) {
						styleClass = getOpenStyleClass();
					} else {
						styleClass = getClosedStyleClass();
					}
				} else {
					styleClass = getOtherStyleClass(t);
				}

				if (isSelected()) {
					styleClass += " " + getSelectedStyleClass();
				}

				return styleClass;
			}

			@Override
			protected void initOrgMenu(WebMarkupContainer orgPanel) {
				AjaxLink edit = new AjaxLink("orgEdit", 
						createStringResource("styledLinkLabel.orgMenu.edit")) {
					@Override
					public void onClick(AjaxRequestTarget target) {

					}
				};
				orgPanel.add(edit);

				AjaxLink rename = new AjaxLink("orgRename",
						createStringResource("styledLinkLabel.orgMenu.rename")) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						//model.getObject().setEditable(!model.getObject().isEditable());
					}
				};
				orgPanel.add(rename);

				AjaxLink createSub = new AjaxLink("orgCreateSub",
						createStringResource("styledLinkLabel.orgMenu.createSub")) {
					@Override
					public void onClick(AjaxRequestTarget target) {

					}
				};
				orgPanel.add(createSub);

				AjaxLink del = new AjaxLink("orgDel", 
						createStringResource("styledLinkLabel.orgMenu.del")) {
					@Override
					public void onClick(AjaxRequestTarget target) {

					}
				};
				orgPanel.add(del);
			}
			
			@Override
			protected void initUserMenu(WebMarkupContainer userPanel) {
				AjaxLink edit = new AjaxLink("userEdit", 
						createStringResource("styledLinkLabel.userMenu.edit")) {
					@Override
					public void onClick(AjaxRequestTarget target) {

					}
				};
				userPanel.add(edit);

				AjaxLink rename = new AjaxLink("userRename",
						createStringResource("styledLinkLabel.userMenu.rename")) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						
					}
				};
				userPanel.add(rename);

				AjaxLink enable = new AjaxLink("userEnable",
						createStringResource("styledLinkLabel.userMenu.enable")) {
					@Override
					public void onClick(AjaxRequestTarget target) {

					}
				};
				userPanel.add(enable);

				AjaxLink disable = new AjaxLink("userDisable", 
						createStringResource("styledLinkLabel.userMenu.disable")) {
					@Override
					public void onClick(AjaxRequestTarget target) {

					}
				};
				userPanel.add(disable);
				
				AjaxLink changeAttr = new AjaxLink("userChangeAttr", 
						createStringResource("styledLinkLabel.userMenu.changeAttr")) {
					@Override
					public void onClick(AjaxRequestTarget target) {

					}
				};
				userPanel.add(changeAttr);
			}

		};

	}

	private List<NodeDto> getNodes(NodeDto parent) {
		OrgStructDto orgUnit = loadOrgUnit(parent.getOid());
		List<NodeDto> listNodes = new ArrayList<NodeDto>();

		if (orgUnit.getOrgUnitDtoList() != null && !orgUnit.getOrgUnitDtoList().isEmpty()) {
			for (Object orgObject : orgUnit.getOrgUnitDtoList()) {
				NodeDto org = (NodeDto) orgObject;
				org.setParent(parent);
				listNodes.add(org);
			}
		}

		if (orgUnit.getUserDtoList() != null && !orgUnit.getUserDtoList().isEmpty()) {
			for (Object userObject : orgUnit.getUserDtoList()) {
				NodeDto user = (NodeDto) userObject;
				user.setParent(parent);
				user.setType(OrgStructDto.getRelation(parent, user.getOrgRefs()));
				listNodes.add(user);
			}
		}
		return listNodes;
	}

	private OrgStructDto loadOrgUnit(String oid) {
		Task task = createSimpleTask(OPERATION_LOAD_ORGUNIT);
		OperationResult result = new OperationResult(OPERATION_LOAD_ORGUNIT);

		OrgStructDto newOrgModel = null;
		List<PrismObject<ObjectType>> orgUnitList;

		OrgFilter orgFilter = OrgFilter.createOrg(oid, null, "1");
		ObjectQuery query = ObjectQuery.createObjectQuery(orgFilter);

		try {
			orgUnitList = getModelService().searchObjects(ObjectType.class, query, null, task, result);
			newOrgModel = new OrgStructDto<ObjectType>(orgUnitList);
			result.recordSuccess();
		} catch (Exception ex) {
			result.recordFatalError("Unable to load org unit", ex);
		}

		if (!result.isSuccess()) {
			showResult(result);
		}

		if (newOrgModel.getOrgUnitDtoList() == null) {
			result.recordFatalError("pageOrgStruct.message.noOrgStructDefined");
			showResult(result);
		}
		return newOrgModel;
	}
}