//
// Created by Administrator on 2018/4/26.
//
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "cm_ctrl.h"


#include <sys/socket.h>
//#include <netinet/in_systm.h>
#include <netinet/ip.h>
//#include <netinet/ip_icmp.h>
//#include <sys/socket.h>
#include <sys/time.h>
//#include <sys/signal.h>
//#include <errno.h>
#include <unistd.h>
//#include <netdb.h>

#include "android/log.h"

char cm_snmp_password_pdu[]=
        {
                0x30, 0x38,
                0x02, 0x01, 0x00,
                0x04,0x07, 0x70, 0x72, 0x69, 0x76, 0x61, 0x74, 0x65,
                0xa3, 0x2a,
                0x02, 0x02, 0x2c, 0x3b,
                0x02, 0x01, 0x00,
                0x02, 0x01, 0x00,
                0x30, 0x1e,
                0x30, 0x1c, 0x06, 0x10, 0x2b, 0x06, 0x01, 0x04, 0x01,
                0xa2,0x3d, 0x02, 0x63, 0x01, 0x01, 0x01, 0x02, 0x01, 0x02, 0x01,
                0x04, 0x08, 0x70, 0x61, 0x73, 0x73, 0x77, 0x6f, 0x72, 0x64
        };
char cm_snmp_get_MacAddr[]=
        {
                0x30,0x31,
                0x02,0x01,0x01,
                0x04,0x07,0x70,0x72,0x69,0x76,0x61,0x74,0x65,
                0xa0,0x23,
                0x02,0x02,0x09,0x80,
                0x02,0x01,0x00,
                0x02,0x01,0x00,
                0x30,0x17,
                0x30,0x15,
                0x06,0x11,
                0x2b,0x06,0x01,0x04,0x01,0xa2,0x3d,0x02,0x63,0x01,0x01,0x02,0x01,0x04,0x01,0x02,0x01,
                0x05,0x00
        };

char cm_snmp_cdPrivateMibEnable[]=
        {
                0x30,0x2f,
                0x02,0x01,0x00,
                0x04,0x07,0x70,0x72,0x69,0x76,0x61,0x74,0x65,
                0xa3,0x21,
                0x02,0x02,0x49,0x5f,
                0x02,0x01,0x00,
                0x02,0x01,0x00,
                0x30,0x15,
                0x30,0x13,
                0x06,0x0e,
                0x2b,0x06,0x01,0x04,0x01,0xa2,0x3d,0x02,0x63,0x01,0x01,0x01,0x01,0x00,
                0x02,0x01,0x00
        };

char cm_snmp_get_status_value[]=
        {0x30,0x2a,0x02,0x01,0x00,0x04,0x07,0x70,0x72,0x69,0x76,0x61,0x74,0x65,0xa1,0x1c,
         0x02,0x02,0x4f,0xa7,0x02,0x01,0x00,0x02,0x01,0x00,0x30,0x10,0x30,0x0e,0x06,0x0a,
         0x2b,0x06,0x01,0x02,0x01,0x0a,0x7f,0x01,0x02,0x02,0x05,0x00};
char cm_mac[CM_MAC_LENGTH];
int CmStatValue;

#define LOG_TAG "RootShell"
#define LOGD(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

/*this is for test*/
int32_t cm_Get_StatusValue()
{
    int ret;
    int32_t tmp;
    ret=cm_send_snmp_command(CM_CMD_PASSWORD);
    ret=cm_send_snmp_command(CM_CMD_GET_STAT_VALUE);
    tmp = (int32_t)CmStatValue;
    return tmp;
}


void cm_Get_MAC_Address(uint8_t *mac)
{
    int ret=0;

    memset(cm_mac, 0, 6);
    ret=cm_send_snmp_command(CM_CMD_PASSWORD);
    ret=cm_send_snmp_command(CM_CMD_GET_MACADDR);
//    ret=cm_send_snmp_command(CM_CMD_PRIVATE_MIBENABLE);
//    LOGD("command[CM_CMD_PRIVATE_MIBENABLE],ret[%d]\n", ret);
    //  sys_thread_del(pthread_id);
    memmove(mac, cm_mac, 6);
}

unsigned int cm_generate_snmp_requestid()
{
    struct timeval tv;
    unsigned int ticks;
    unsigned int ret;

    gettimeofday(&tv, NULL);
    ticks = tv.tv_sec + tv.tv_usec;
    srand(ticks);
    ret = (rand() % 31765) + 1000;

    return ret;
}

int cm_send_snmp_command(int command)
{
    int server_port;
    int ret;
    int len;
    int socketfd;
    int nbytes;
    fd_set rset;
    struct sockaddr_in remote_addr;
    struct sockaddr_in remote_addr1;
    struct timeval tmo;
    char recvbuf[256];
    unsigned int requestid;
    int retry_count=0;

    server_port = SNMP_PORT;

    //memset(&remote_addr, 0, sizeof(remote_addr));
    remote_addr.sin_family = AF_INET;
    remote_addr.sin_addr.s_addr = htonl(0xc0a86401);
    remote_addr.sin_port = htons(server_port);

    len = sizeof(remote_addr);

    socketfd = socket(AF_INET, SOCK_DGRAM, 0);
    if(socketfd < 0)
    {
        //socket error
        return -1;
    }

    requestid = cm_generate_snmp_requestid();
#if CM_SNMP_DEBUG
    LOGD("[%s] %s -- request id is %x \n", CM_SNMP_DEBUG_PREFIX, __FUNCTION__, requestid);
#endif

    retry_count = 0;
    while(retry_count < CM_CMD_RETRY_NUM)
    {
        /* stop or start cmd */
        if(command == CM_CMD_PASSWORD)
        {
            cm_snmp_password_pdu[CM_PASSWORD_OFFSET-1] = ((requestid>>8) & 0x000000ff);
            cm_snmp_password_pdu[CM_PASSWORD_OFFSET] = (requestid & 0x000000ff);
            nbytes = sendto(socketfd, cm_snmp_password_pdu, sizeof(cm_snmp_password_pdu), 0,
                            (struct sockaddr *)&remote_addr, len);

        }
        else if(command==CM_CMD_GET_MACADDR)
        {
            cm_snmp_get_MacAddr[CM_FREQUENCY_OFFSET-1] = ((requestid>>8) & 0x000000ff);
            cm_snmp_get_MacAddr[CM_FREQUENCY_OFFSET] = (requestid & 0x000000ff);

            nbytes = sendto(socketfd, cm_snmp_get_MacAddr, sizeof(cm_snmp_get_MacAddr), 0,
                            (struct sockaddr *)&remote_addr, len);
        }
        else if(command==CM_CMD_PRIVATE_MIBENABLE)
        {
            cm_snmp_cdPrivateMibEnable[CM_FREQUENCY_OFFSET-1] = ((requestid>>8) & 0x000000ff);
            cm_snmp_cdPrivateMibEnable[CM_FREQUENCY_OFFSET] = (requestid & 0x000000ff);

            nbytes = sendto(socketfd, cm_snmp_cdPrivateMibEnable, sizeof(cm_snmp_cdPrivateMibEnable), 0,
                            (struct sockaddr *)&remote_addr, len);
        }
        else if(command==CM_CMD_GET_STAT_VALUE)
        {
            cm_snmp_get_status_value[CM_FREQUENCY_OFFSET-1] = ((requestid>>8) & 0x000000ff);
            cm_snmp_get_status_value[CM_FREQUENCY_OFFSET] = (requestid & 0x000000ff);
            nbytes = sendto(socketfd, cm_snmp_get_status_value, sizeof(cm_snmp_get_status_value), 0,
                            (struct sockaddr *)&remote_addr, len);
        }

//NoCompare://如果在判断语句中添加直接跳转的功能，可以减少部分时间开销，暂时还不添加wlei

        if(nbytes <=0)
        {
            //send error;
#if CM_SNMP_DEBUG
            LOGD("[%s] %s -- send data return error [nbytes = %d] \n", CM_SNMP_DEBUG_PREFIX, __FUNCTION__, nbytes);
#endif
            close(socketfd);
            return -1;
        }

        tmo.tv_sec = 2;
         tmo.tv_usec = 500*1000; /* 100 m-seconds */
        //tmo.tv_usec = 0;

        FD_ZERO(&rset);
        FD_SET(socketfd, &rset);

        ret = 0;
        ret = select(socketfd+1, &rset, NULL, NULL, &tmo);

        if(ret < 0)
        {
            //recv error or timeout
#if CM_SNMP_DEBUG
            LOGD("[%s] %s -- select return error [ret = %d] \n", CM_SNMP_DEBUG_PREFIX, __FUNCTION__, ret);
#endif
            close(socketfd);
            return -1;
        }
        else if(ret == 0)
        {
#if CM_SNMP_DEBUG
            LOGD("[%s] %s -- (ret == 0)\n", CM_SNMP_DEBUG_PREFIX, __FUNCTION__);
#endif
            retry_count++;
            continue;
        }
        /*sendto 的最后一个参数是整数值，而 recvfrom 的最后一个参数是一个指向整数值的指针，注意一下*/
        nbytes = recvfrom(socketfd, recvbuf, 256, 0,
                          (struct sockaddr *)&remote_addr1, &len);

        if(nbytes <= 0)
        {
            //recv error or no data
#if CM_SNMP_DEBUG
            LOGD("[%s] %s -- recv return error [nbytes = %d] \n", CM_SNMP_DEBUG_PREFIX, __FUNCTION__, nbytes);
#endif
            close(socketfd);
            return -1;
        }
        else
        { //calculate response id
            if(((recvbuf[CM_PASSWORD_OFFSET-1] & 0x000000ff) == ((requestid >> 8) & 0x000000ff)) &&
               ((recvbuf[CM_PASSWORD_OFFSET] & 0x000000ff)== (requestid & 0x000000ff)))
            {
                if(command==CM_CMD_GET_MACADDR)
                {
                    memcpy(cm_mac,&recvbuf[CM_DSCHANNEL_OFFSET-1],6);
                }
                else if(command==CM_CMD_GET_STAT_VALUE)
                {
                    CmStatValue = 0x00000000;
                    memcpy(&CmStatValue,(char *)&recvbuf[CM_STAT_VALUE_OFFSET-1] ,1);
                }
            //it's ok?
                break;
            }
            else
            {
                //data error
#if CM_SNMP_DEBUG
                LOGD("[%s] %s -- recv data error [nbytes = %d] \n", CM_SNMP_DEBUG_PREFIX, __FUNCTION__, nbytes);
#endif
                retry_count++;
                continue;
            }
        }
    }

    //it's ok?
    if(retry_count >= CM_CMD_RETRY_NUM)
    {
        ret = -1;
    }
    else
    {
        ret = 0;
    }
    close(socketfd);

    return ret;

}