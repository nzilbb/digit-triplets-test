export interface Instance {
    instance_id: string;
    other_instance_id: string;
    user_agent: string;
    ip: string;
    start_time: string;
    end_time: string;
    trial_set_id: number;
    test_result: number;
    mean_snr: number;
    mode: string;
}
