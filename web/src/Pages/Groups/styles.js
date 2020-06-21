import { makeStyles } from '@material-ui/core/styles';
import {blue} from "@material-ui/core/colors";

export const useStyles = makeStyles({
    table: {
        minWidth: 650,
    },
    activeTable: {
        backgroundColor: blue[100],
    }
});