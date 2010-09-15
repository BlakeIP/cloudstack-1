/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
 * 
 * This software is licensed under the GNU General Public License v3 or later.
 * 
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.cloud.api.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloud.api.ApiDBUtils;
import com.cloud.api.BaseListCmd;
import com.cloud.api.Implementation;
import com.cloud.api.Parameter;
import com.cloud.api.response.VlanIpRangeResponse;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.Vlan.VlanType;
import com.cloud.dc.VlanVO;
import com.cloud.serializer.SerializerHelper;
import com.cloud.user.Account;

@Implementation(method="searchForVlans")
public class ListVlanIpRangesCmd extends BaseListCmd {
	public static final Logger s_logger = Logger.getLogger(ListVlanIpRangesCmd.class.getName());

    private static final String s_name = "listvlaniprangesresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name="account", type=CommandType.STRING)
    private String accountName;

    @Parameter(name="domainid", type=CommandType.LONG)
    private Long domainId;

    @Parameter(name="id", type=CommandType.LONG, required=true)
    private Long id;

    @Parameter(name="podid", type=CommandType.LONG)
    private Long podId;

    @Parameter(name="vlan", type=CommandType.STRING)
    private String vlan;

    @Parameter(name="zoneid", type=CommandType.LONG)
    private Long zoneId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getAccountName() {
        return accountName;
    }

    public Long getDomainId() {
        return domainId;
    }

    public Long getId() {
        return id;
    }

    public Long getPodId() {
        return podId;
    }

    public String getVlan() {
        return vlan;
    }

    public Long getZoneId() {
        return zoneId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getName() {
        return s_name;
    }

    @Override @SuppressWarnings("unchecked")
    public String getResponse() {
        List<VlanVO> vlans = (List<VlanVO>)getResponseObject();

        List<VlanIpRangeResponse> response = new ArrayList<VlanIpRangeResponse>();
        for (VlanVO vlan : vlans) {
            Long accountId = ApiDBUtils.getAccountIdForVlan(vlan.getId());
            Long podId = ApiDBUtils.getPodIdForVlan(vlan.getId());

            VlanIpRangeResponse vlanResponse = new VlanIpRangeResponse();
            vlanResponse.setId(vlan.getId());
            vlanResponse.setForVirtualNetwork(vlan.getVlanType().equals(VlanType.VirtualNetwork));
            vlanResponse.setVlan(vlan.getVlanId());
            vlanResponse.setZoneId(vlan.getDataCenterId());
            
            if (accountId != null) {
                Account account = ApiDBUtils.findAccountById(accountId);
                vlanResponse.setAccountName(account.getAccountName());
                vlanResponse.setDomainId(account.getDomainId());
                vlanResponse.setDomainName(ApiDBUtils.findDomainById(account.getDomainId()).getName());
            }

            if (podId != null) {
                HostPodVO pod = ApiDBUtils.findPodById(podId);
                vlanResponse.setPodId(podId);
                vlanResponse.setPodName(pod.getName());
            }

            vlanResponse.setGateway(vlan.getVlanGateway());
            vlanResponse.setNetmask(vlan.getVlanNetmask());
            vlanResponse.setDescription(vlan.getIpRange());

            response.add(vlanResponse);
        }

        return SerializerHelper.toSerializedString(response);
    }
}
