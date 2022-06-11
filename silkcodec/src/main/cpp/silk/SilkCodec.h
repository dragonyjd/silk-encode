/**
 * 支持边录边解，边解边录=
 *
 * @author guoxiaolong
 * @date 2019-09-20
 */

#ifndef SILK_ENCODER_H
#define SILK_ENCODER_H

#ifdef __cplusplus
extern "C" {
#endif

int Silk_Encoder_Init();
int Silk_Encoder_Encode(short * in, int inLen, unsigned char * out, int outLen);
double Silk_Encoder_Finish();

int Silk_Decoder(const char* filename);
void Silk_Decoder_Reset();

double Silk_Get_Record_Length(const char* filename);

#ifdef __cplusplus
}
#endif

#endif //SILK_ENCODER_H
