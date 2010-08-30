/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package social.portal.webui.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceUtils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;


/** 
 * customized from UIApplicationList <br />
 * Created by The eXo Platform SAS
 * @author <a href="mailto:hoatlevan@gmail.com">hoatle</a>
 * @since Nov 09, 2009
 */
@ComponentConfig(
  template = "app:/groovy/portal/webui/component/UISpaceApplicationList.gtmpl",
  events = {
      @EventConfig(listeners = UISpaceApplicationList.SelectCategoryActionListener.class),
      @EventConfig(listeners = UISpaceApplicationList.InstallApplicationActionListener.class)
  }
)
public class UISpaceApplicationList extends UIContainer implements UIPopupComponent {
  private Map<ApplicationCategory, List<Application>> appStore;
  private ApplicationCategory selectedCategory;
  private Space space;
  
  /**
   * constructor
   */
  public UISpaceApplicationList() {
   
  }
  
  /**
   * Gets selected category
   * @return selected category
   */
  public ApplicationCategory getSelectedCategory() {
    return selectedCategory;
  }
  
  /**
   * Gets default selected category in case no category is selected for template.
   * The default selected category is the first key element in the appStore map.
   * @return default selected category
   */
  public ApplicationCategory getDefaultSelectedCategory() {
    if (appStore.keySet().size() > 0) {
      return appStore.keySet().iterator().next();
    }
    return null;
  }
  
  /**
   * Sets selected category
   * @param categoryName
   */
  public void setSelectedCategory(String categoryName) {
   Iterator<ApplicationCategory> categoryItr = appStore.keySet().iterator();
   while (categoryItr.hasNext()) {
     ApplicationCategory category = categoryItr.next();
     if (category.getName().equals(categoryName)) {
       selectedCategory = category;
     }
   }
  }
  
  /**
   * Sets space
   * @param space
   * @throws Exception
   */
  public void setSpace(Space space) throws Exception {
    this.space = space;
    appStore = new LinkedHashMap<ApplicationCategory, List<Application>>();
    Map<ApplicationCategory, List<Application>> gotAppStore = SpaceUtils.getAppStore(space);
    Iterator<Entry<ApplicationCategory, List<Application>>> entrySetItr = gotAppStore.entrySet().iterator();
    ApplicationCategory appCategory;
    List<Application> appList;
    while(entrySetItr.hasNext()) {
      Entry<ApplicationCategory, List<Application>> entrySet = entrySetItr.next();
      appCategory = entrySet.getKey();
      appList = entrySet.getValue();
      Iterator<Application> appItr = appList.iterator();
      List<Application> tempAppList = new ArrayList<Application>();
      while (appItr.hasNext()) {
        Application app = appItr.next();
        String appStatus = SpaceUtils.getAppStatus(space, app.getApplicationName());
//        if (appStatus != null) {
//          if (!appStatus.equals(Space.ACTIVE_STATUS)) {
//            tempAppList.add(app);
//          }
//        } else {
//          tempAppList.add(app);
//        }
        tempAppList.add(app);
      }
      if (tempAppList.size() > 0) {
        appStore.put(appCategory, tempAppList);
      }
    }
  }
  /**
   * Gets space
   * @return space
   */
  public Space getSpace() {
    return space;
  }
  
  /**
   * Gets appStore
   * @return appStore
   */
  public Map<ApplicationCategory, List<Application>> getAppStore() {
    return appStore;
  }
 
  /**
   * Triggers this action when user selects on category
   * @author hoatle
   *
   */
  static public class SelectCategoryActionListener extends EventListener<UISpaceApplicationList> {

    @Override
    public void execute(Event<UISpaceApplicationList> event) throws Exception {
      String selectedCategory = event.getRequestContext().getRequestParameter(OBJECTID);
      UISpaceApplicationList uiSpaceAppList = event.getSource();
      uiSpaceAppList.setSelectedCategory(selectedCategory);
    }
    
  }
  
  /**
   * Triggers this action when user clicks on install button
   * @author hoatle
   */
  static public class InstallApplicationActionListener extends EventListener<UISpaceApplicationList> {
    @Override
    public void execute(Event<UISpaceApplicationList> event) throws Exception {
      String appId = event.getRequestContext().getRequestParameter(OBJECTID);
      UISpaceApplicationList uiSpaceAppList = event.getSource();
      SpaceService spaceService = uiSpaceAppList.getApplicationComponent(SpaceService.class);
      spaceService.installApplication(uiSpaceAppList.space, appId);
      spaceService.activateApplication(uiSpaceAppList.space, appId);
      uiSpaceAppList.setSpace(uiSpaceAppList.space);
      UISpaceApplication uiSpaceApp = (UISpaceApplication) uiSpaceAppList.getAncestorOfType(UISpaceApplication.class);
      uiSpaceApp.setValue(uiSpaceAppList.space);
      SpaceUtils.updateWorkingWorkSpace();
    }
    
  }
  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {    
  }
  
}