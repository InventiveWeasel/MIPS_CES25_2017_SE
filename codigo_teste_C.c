#include <stdio.h>

int main(){
    int r[32], mem[4096];
    int i;

    for (i = 0; i < 32; i++)
        r[i] = 0;
    for (i = 0; i < 4096; i++)
        mem[i] = 0;

    // CÓDIGO PRINCIPAL ===============================
    r[0] = r[0] + 1; // addi
    r[1] = r[1] + 1; // addi
    r[3] = r[3] + 2; // addi
    r[10] = r[10] + 10; // addi
    r[11] = r[11] + 55; // addi
    // nop
    label1:
    r[2] = r[0] + r[1]; // add
    r[0] = r[1] + 0; // addi
    r[1] = r[2] + 0; // addi
    r[3] = r[3] + 1; // addi
    if (r[3] <= r[10]) goto label1; // ble
    if (r[3] != r[11]) goto label2; // bne
    mem[r[3]+5] = r[3]; //sw
    goto label3; // jmp
    label2:
    mem[r[3]+5] = r[0]; // sw
    label3:
    r[4] = mem[r[3]+5]; // lw
    r[13] = r[13] + 2; // addi
    r[5] = r[4] * r[13]; // mul
    // nop
    // nop
    r[7] = r[7] + 4; // addi
    r[14] = r[14] + 5; // addi
    r[15] = r[15] + 85; // addi
    label4:
    r[5] = r[5] - r[14]; // sub
    r[8] = r[8] + 1; // addi
    if (r[8] <= r[7]) goto label4; // ble
    if (r[5] == r[15]) goto label5; // beq
    goto label6; // jmp
    label5:
    r[31] = r[31] + 1; // addi
    label6:
    r[30] = r[30] + 1; // addi
    // ================================================

    for (i = 0; i < 32; i++)
        printf("r[%d]: %d\n", i, r[i]);

    return 0;
}
