#ifndef CM_CTRL_H
#define CM_CTRL_H

#include <string.h>
#define SNMP_PORT 161

#define CM_CMD_PASSWORD     3
//#define CM_CMD_FACTORY        4
#define CM_CMD_RETRY_NUM    4

/*----------------------------------------------------------------------------*/
#define CM_CMD_GET_MACADDR  24
#define CM_CMD_PRIVATE_MIBENABLE 25
#define CM_CMD_GET_STAT_VALUE   50
#define CM_STAT_VALUE_OFFSET    48

/*----------------CM channel extend info define----------------------------*/

/*----------------设置CM参数命令定义----------------------------*/
#define CM_CMD_SET_MACADDR  90 //设置CM的MAC地址
#define CM_MACADDR_OFFSET   52 //MAC地址字段偏移


/*---------------------------request id 偏移----------------------------------*/

#define CM_PASSWORD_OFFSET      19
#define CM_FACTORY_OFFSET         19
#define CM_SCANNING_OFFSET       19
#define CM_FREQUENCY_OFFSET     19
#define CM_LENGHT_OFFSET           1
#define CM_DSCHANNEL_OFFSET   52

#define CM_REQUEST_ID_OFFSET        19
#define CM_REQUEST_ID_EQDATA_OFFSET        21

/*------------response报文中绑定变量的结果字段偏移----------------------------*/
/*------一般绑定变量的结果字段偏移为48，一些特殊类型的变量偏移单独定义--------*/
/*----------------------------------------------------------------------------*/

/*供构造snmp数据包使用，数据类型常用*/

/*----------------------------------------------------------------------------*/

#define CM_SNMP_DEBUG 1
#define CM_SNMP_DEBUG_PREFIX    "CM-SNMP-DEBUG"
#define CM_MAC_LENGTH  6

typedef unsigned char uint8_t;
typedef unsigned short uint16_t;
typedef unsigned int uint32_t;
typedef short int16_t;
typedef int int32_t;

typedef struct _STB_CM_Status_info_t_  //CM状态信息
{
    int32_t  CmStatValue;  //docsIfCmStatusValue
    uint32_t CmStatResets; //docsIfCmStatusResets
    uint32_t CmStatLostSyncs;//docsIfCmStatusLostSyncs
    uint32_t CmStatInvMaps;//docsIfCmStatusInvalidMaps
    uint32_t CmStatInvUcds;//docsIfCmStatusInvalidUcds
    uint32_t CmStatInvRangRes; //docsIfCmStatusInvalidRangingResponses
    uint32_t CmStatInvRegRes; //docsIfCmStatusInvalidRegistrationResponses

    uint32_t CmStatT1Timeout; //docsIfCmStatusT1Timeouts
    uint32_t CmStatT2Timeout; //docsIfCmStatusT2Timeouts
    uint32_t CmStatT3Timeout; //docsIfCmStatusT3Timeouts
    uint32_t CmStatT4Timeout; //docsIfCmStatusT4Timeouts
    uint32_t CmStatRangAbr; //docsIfCmStatusRangingAborteds
    int32_t  CmStatDocOperMode; //docsIfCmStatusDocsisOperMode
    int32_t  CmStatModuType;    //docsIfCmStatusModulationType
    uint8_t  CmStatusEqData[101]; //docsIfCmStatusEqualizationData
    uint8_t  CmStatCode[10]; //docsIfCmStatusCode
    
#if _IS_ADD_ATTRIBUTE_
}__attribute__((packed)) STB_CM_Status_info_t, *pSTB_CM_Status_info_t;
#else 
} STB_CM_Status_info_t, *pSTB_CM_Status_info_t;
#endif

typedef struct _STB_CM_info_ext_t_  //CM扩展信息
{
    /*********UP***************/
    int32_t  UpWidth; //docsIfUpChannelWidth
    uint32_t UpSlotSize; //docsIfUpChannelSlotSize
    uint32_t UpTxTimOffset; //docsIfUpChannelTxTimingOffset
    int32_t  UpRangBackStart; //docsIfUpChannelRangingBackoffStart
    int32_t  UpRangBackEnd; //docsIfUpChannelRangingBackoffEnd
    int32_t  UpTxBackStart; //docsIfUpChannelTxBackoffStart
    int32_t  UpTxBackEnd; //docsIfUpChannelTxBackoffEnd
    uint32_t UpScdmaActCodes; //docsIfUpChannelScdmaActiveCodes
    int32_t  UpScdmaCodesPerSlot; //docsIfUpChannelScdmaCodesPerSlot
    uint32_t UpScdmaFraSize; //docsIfUpChannelScdmaFrameSize
    uint32_t UpScdmaHopSeed; //docsIfUpChannelScdmaHoppingSeed
    int32_t  UpType; //docsIfUpChannelType
    int32_t  UpCloFrom; //docsIfUpChannelCloneFrom
    int32_t  UpUpdate; //docsIfUpChannelUpdate
    int32_t  UpStatus; //docsIfUpChannelStatus
    int32_t  UpPreEqEnable; //docsIfUpChannelPreEqEnable

    /*********DOWN***************/
    int32_t  DownWidth; //docsIfDownChannelWidth
    int32_t  DownInterleave; //docsIfDownChannelInterleave      
#if _IS_ADD_ATTRIBUTE_
}__attribute__((packed)) STB_CM_info_ext_t, *pSTB_CM_info_ext_t;
#else 
} STB_CM_info_ext_t, *pSTB_CM_info_ext_t;
#endif

typedef struct _STB_CM_info_t_  //CM信号参数信息
{
    uint32_t cur_frequency; //当前频率
    int16_t cm_signal_strength; //接收电平
    uint16_t cm_modulation; //调制方式
    uint32_t cm_snr; //信噪比
    uint32_t  channel_id; //通道ID
#if _IS_ADD_ATTRIBUTE_
}__attribute__((packed)) STB_CM_info_t, *pSTB_CM_info_t;
#else 
} STB_CM_info_t, *pSTB_CM_info_t;
#endif

typedef struct _STB_CM_info_desc_t_     //CM描述信息
{
    uint8_t desc_tag; //Cable Modem参数信息描述子（AMEP_CM）
    uint8_t desc_len; //sizeof(STB_CM_info_desc_t) - 2
    //time_info_t time; //实际发生时间
    STB_CM_info_t cm_down; //CM 下行信号参数
    STB_CM_info_t cm_up; //CM 上行信号参数
    uint8_t cm_mac[CM_MAC_LENGTH];//CM 的MAC地址
    uint8_t send_mode; //发送模式,0-report sending,1-query sending,2-warning sending
    STB_CM_Status_info_t cm_status; //CM状态信息
    STB_CM_info_ext_t cm_extend;   //CM扩展信息
#if _IS_ADD_ATTRIBUTE_
}__attribute__((packed)) STB_CM_info_desc_t, *pSTB_CM_info_desc_t;
#else 
} STB_CM_info_desc_t, *pSTB_CM_info_desc_t;
#endif



struct snmp_get_request_pdu
{

};


/*-----------Get up channel information -----------------*/

/*--------------Get down channel information-----------------*/

/*----------Get CM Status information-------------------------*/

/*------------------------------------------------------------*/    

void cm_Get_MAC_Address(uint8_t *mac);

//struct STB_CM_info_t* cm_Get_Up_Info();
//struct STB_CM_info_t* cm_Get_Down_Info();

#endif



