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

/*----------------����CM���������----------------------------*/
#define CM_CMD_SET_MACADDR  90 //����CM��MAC��ַ
#define CM_MACADDR_OFFSET   52 //MAC��ַ�ֶ�ƫ��


/*---------------------------request id ƫ��----------------------------------*/

#define CM_PASSWORD_OFFSET      19
#define CM_FACTORY_OFFSET         19
#define CM_SCANNING_OFFSET       19
#define CM_FREQUENCY_OFFSET     19
#define CM_LENGHT_OFFSET           1
#define CM_DSCHANNEL_OFFSET   52

#define CM_REQUEST_ID_OFFSET        19
#define CM_REQUEST_ID_EQDATA_OFFSET        21

/*------------response�����а󶨱����Ľ���ֶ�ƫ��----------------------------*/
/*------һ��󶨱����Ľ���ֶ�ƫ��Ϊ48��һЩ�������͵ı���ƫ�Ƶ�������--------*/
/*----------------------------------------------------------------------------*/

/*������snmp���ݰ�ʹ�ã��������ͳ���*/

/*----------------------------------------------------------------------------*/

#define CM_SNMP_DEBUG 1
#define CM_SNMP_DEBUG_PREFIX    "CM-SNMP-DEBUG"
#define CM_MAC_LENGTH  6

typedef unsigned char uint8_t;
typedef unsigned short uint16_t;
typedef unsigned int uint32_t;
typedef short int16_t;
typedef int int32_t;

typedef struct _STB_CM_Status_info_t_  //CM״̬��Ϣ
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

typedef struct _STB_CM_info_ext_t_  //CM��չ��Ϣ
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

typedef struct _STB_CM_info_t_  //CM�źŲ�����Ϣ
{
    uint32_t cur_frequency; //��ǰƵ��
    int16_t cm_signal_strength; //���յ�ƽ
    uint16_t cm_modulation; //���Ʒ�ʽ
    uint32_t cm_snr; //�����
    uint32_t  channel_id; //ͨ��ID
#if _IS_ADD_ATTRIBUTE_
}__attribute__((packed)) STB_CM_info_t, *pSTB_CM_info_t;
#else 
} STB_CM_info_t, *pSTB_CM_info_t;
#endif

typedef struct _STB_CM_info_desc_t_     //CM������Ϣ
{
    uint8_t desc_tag; //Cable Modem������Ϣ�����ӣ�AMEP_CM��
    uint8_t desc_len; //sizeof(STB_CM_info_desc_t) - 2
    //time_info_t time; //ʵ�ʷ���ʱ��
    STB_CM_info_t cm_down; //CM �����źŲ���
    STB_CM_info_t cm_up; //CM �����źŲ���
    uint8_t cm_mac[CM_MAC_LENGTH];//CM ��MAC��ַ
    uint8_t send_mode; //����ģʽ,0-report sending,1-query sending,2-warning sending
    STB_CM_Status_info_t cm_status; //CM״̬��Ϣ
    STB_CM_info_ext_t cm_extend;   //CM��չ��Ϣ
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



