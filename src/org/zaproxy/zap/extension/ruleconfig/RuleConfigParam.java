/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2016 The ZAP Development team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.ruleconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.parosproxy.paros.common.AbstractParam;

/**
 * Manages the Rule Configurations saved in the configuration file.
 * <p>
 * It also exposes the names of the (core) rules that can be accessed by the scanners (through the {@code Configuration}
 * set/used by the passive/active scans).
 * 
 * @since TODO add version
 */
public class RuleConfigParam extends AbstractParam {

    /**
     * The name of the rule to obtain the time, in seconds, for time-based attacks.
     */
    public static final String RULE_COMMON_SLEEP_TIME = "rules.common.sleep";

    /**
     * The name of the rule to obtain the string containing a comma separated list of names/IDs of forms that should be ignored
     * when checking for CSRF issues.
     */
    public static final String RULE_CSRF_IGNORE_LIST = "rules.csrf.ignorelist";

    /**
     * The name of the rule to obtain the string containing a comma separated list of cookies that should be ignored
     * when checking for issues.
     */
    public static final String RULE_COOKIE_IGNORE_LIST = "rules.cookie.ignorelist";

    private static final String RULES_BASE_KEY = "rules";
    private static final String RULES_DEFAULT_KEY_EXT = ".default";
    
    private Map<String, RuleConfig> ruleConfigs = new HashMap<String, RuleConfig>();

    public RuleConfigParam() {
    }

    @Override
    protected void parse() {
        // Add the built in rule configs
        this.addRuleConfig(new RuleConfig(RULE_COMMON_SLEEP_TIME, "20"));
        this.addRuleConfig(new RuleConfig(RULE_CSRF_IGNORE_LIST, ""));
        this.addRuleConfig(new RuleConfig(RULE_COOKIE_IGNORE_LIST, ""));
        
        Iterator<String> iter = this.getConfig().getKeys(RULES_BASE_KEY);
        RuleConfig rc;
        while (iter.hasNext()) {
            String key = iter.next();
            if (ruleConfigs.containsKey(key)) {
                ruleConfigs.get(key).setValue(this.getConfig().getString(key));
            } else {
                // Rules can specify an optional default value in the config file
                rc = new RuleConfig(
                    key, 
                    this.getConfig().getString(key + RULES_DEFAULT_KEY_EXT, ""),
                    this.getConfig().getString(key));
                this.ruleConfigs.put(rc.getKey(), rc);
            }
        }
    }

    public void addRuleConfig (RuleConfig rc) {
        this.ruleConfigs.put(rc.getKey(), rc);
        if (! this.getConfig().containsKey(rc.getKey())) {
            // Dont overwrite an existing value
            this.getConfig().setProperty(rc.getKey(), rc.getValue());
        }
    }

    public void addRuleConfig (String key, String defaultValue, String value) {
        this.ruleConfigs.put(key, new RuleConfig(key, defaultValue, value));
        if (! this.getConfig().containsKey(key)) {
            // Dont overwrite an existing value
            this.getConfig().setProperty(key, value);
        }
    }
    
    public RuleConfig getRuleConfig (String key) {
        if (ruleConfigs.containsKey(key)) {
            return ruleConfigs.get(key).clone();
        }
        return null;
    }
    
    public List<RuleConfig> getAllRuleConfigs () {
        ArrayList<RuleConfig> list = new ArrayList<RuleConfig>();
        for (RuleConfig rc : ruleConfigs.values()) {
            list.add(rc.clone());
        }
        return list;
    }
    
    public String getRuleConfigValue (String key) {
        if (ruleConfigs.containsKey(key)) {
            return ruleConfigs.get(key).getValue();
        }
        return null;
    }
    
    public String getRuleConfigDefaultValue (String key) {
        if (ruleConfigs.containsKey(key)) {
            return ruleConfigs.get(key).getDefaultValue();
        }
        return null;
    }
    
    public void setRuleConfigValue(String key, String value) {
        if (ruleConfigs.containsKey(key)) {
            ruleConfigs.get(key).setValue(value);
            this.getConfig().setProperty(key, value);
        } else {
            throw new IllegalArgumentException("No such key");
        }
    }

    public void resetRuleConfigValue(String key) {
        if (ruleConfigs.containsKey(key)) {
            String value = ruleConfigs.get(key).getDefaultValue();
            ruleConfigs.get(key).setValue(value);
            this.getConfig().setProperty(key, value);
        } else {
            throw new IllegalArgumentException("No such key");
        }
    }

    public void resetAllRuleConfigValues() {
        for (RuleConfig rc : ruleConfigs.values()) {
            rc.reset();
            this.getConfig().setProperty(rc.getKey(), rc.getValue());
        }
    }
}
