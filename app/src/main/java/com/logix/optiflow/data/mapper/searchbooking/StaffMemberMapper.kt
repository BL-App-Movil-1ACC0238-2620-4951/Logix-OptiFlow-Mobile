package com.logix.optiflow.data.mapper.searchbooking

import com.logix.optiflow.data.remote.dto.searchbooking.StaffMemberDto
import com.logix.optiflow.domain.model.searchbooking.StaffMember

fun StaffMemberDto.toDomain(): StaffMember =
    StaffMember(
        id = id,
        opticalStoreId = opticalStoreId,
        name = name,
        email = email,
        role = role,
    )
