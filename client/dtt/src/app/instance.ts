export interface Instance {
    id: string;
    numTrials: number;
    mode: string; // headphones = "l" or "r", speakers = ""
    nextField: number;
    fields: {};
    trialCount: number;
}
