package org.cbioportal.pdb_annotation.web.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.cbioportal.pdb_annotation.web.domain.AlignmentRepository;
import org.cbioportal.pdb_annotation.web.domain.EnsemblRepository;
import org.cbioportal.pdb_annotation.web.domain.GeneSequenceRepository;
import org.cbioportal.pdb_annotation.web.domain.PdbRepository;
import org.cbioportal.pdb_annotation.web.domain.UniprotRepository;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.Ensembl;
import org.cbioportal.pdb_annotation.web.models.GenomeResidue;
import org.cbioportal.pdb_annotation.web.models.GenomeResidueInput;
import org.cbioportal.pdb_annotation.web.models.Residue;
import org.cbioportal.pdb_annotation.web.models.Uniprot;
import org.cbioportal.pdb_annotation.web.models.api.Quote;
import org.cbioportal.pdb_annotation.web.models.api.Transcript_consequences;
import org.cbioportal.pdb_annotation.web.models.api.UtilAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;



/**
 *
 * Controller of the main API: AlignmentController
 *
 * @author Juexin Wang
 *
 */
@RestController // shorthand for @Controller, @ResponseBody
@CrossOrigin(origins = "*") // allow all cross-domain requests
@RequestMapping(value = "/g2s/")
public class AlignmentController {
    @Autowired
    private AlignmentRepository alignmentRepository;
    @Autowired
    private GeneSequenceRepository geneSequenceRepository;
    @Autowired
    private EnsemblRepository ensemblRepository;
    @Autowired
    private UniprotRepository uniprotRepository;
    @Autowired
    private PdbRepository pdbRepository;

    //Query from seqId
    @ApiOperation(value = "get pdb alignments by seqId", nickname = "getPdbAlignmentByGeneSequenceId")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Alignment.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/GeneSeqStructureMappingQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<Alignment> getPdbAlignmentByGeneSequenceId (
            @RequestParam @ApiParam(value = "Input sequence id. For example 1233", required = true, allowMultiple = true) String seqId) {
        return alignmentRepository.findBySeqId(seqId);
    }

    @ApiOperation(value = "get whether seq exists by seqId", nickname = "getExistedSeqIdinAlignment")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Alignment.class, responseContainer = "boolean"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/GeneSeqRecognitionQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public boolean getExistedSeqIdinAlignment(
            @RequestParam @ApiParam(value = "Input seqId. For example 1233", required = true, allowMultiple = true) String seqId) {
        return geneSequenceRepository.findBySeqId(seqId).size() != 0;
    }
    
    
    @ApiOperation(value = "get residue mapping from pdb alignments by seqId and Amino Acid Number", nickname = "getPdbResidueBySeqId")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Alignment.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/GeneSeqResidueMappingQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<Residue> getPdbResidueBySeqId (
            @RequestParam(required = true)
            @ApiParam(value = "Input seq id. Example: 1233", required = true, allowMultiple = true) String seqId, 
            @RequestParam(required = true)
            @ApiParam(value = "Input Amio Acid Number. Example: 42", required = true, allowMultiple = true) String aaNumber) {
        List<Alignment> it = alignmentRepository.findBySeqId(seqId);
        List<Residue> outit = new ArrayList<Residue> ();
        int inputAA = Integer.parseInt(aaNumber);
        for(Alignment ali:it){
            if(inputAA>=ali.getSeqFrom() && inputAA<=ali.getSeqTo()){
                Residue re = new Residue(); 
                re.setAlignmentId(ali.getAlignmentId());
                re.setBitscore(ali.getBitscore());
                re.setChain(ali.getChain());
                re.setSeqAlign(ali.getSeqAlign());
                re.setSeqFrom(ali.getSeqFrom());
                re.setSeqId(ali.getSeqId());
                re.setSeqTo(ali.getSeqTo());
                re.setEvalue(ali.getEvalue());
                re.setIdentity(ali.getIdentity());
                re.setIdentp(ali.getIdentp());
                re.setMidlineAlign(ali.getMidlineAlign());
                re.setPdbAlign(ali.getPdbAlign());
                re.setPdbFrom(ali.getPdbFrom());
                re.setPdbId(ali.getPdbId());
                re.setPdbNo(ali.getPdbNo());
                re.setPdbSeg(ali.getPdbSeg());
                re.setPdbTo(ali.getPdbTo());
                re.setUpdateDate(ali.getUpdateDate());                              
                re.setResidueName(ali.getPdbAlign().substring(inputAA-ali.getSeqFrom(), inputAA-ali.getSeqFrom()+1));
                re.setResidueNum(ali.getPdbFrom()+(inputAA-ali.getSeqFrom())); 
                outit.add(re);
            }
        }
        return outit;
    }
    
    
    //Query from EnsemblId
    @ApiOperation(value = "get pdb alignments by ensemblId", nickname = "getPdbAlignmentByEnsemblId")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Alignment.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/EnsemblStructureMappingQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<Alignment> getPdbAlignmentByEnsemblId (
            @RequestParam @ApiParam(value = "Input ensembl Id. For example ENSP00000489609.1", required = true, allowMultiple = true) String ensemblId) {
        List<Ensembl> ensembllist = ensemblRepository.findByEnsemblId(ensemblId); 
        if(ensembllist.size()==1){
            return alignmentRepository.findBySeqId(ensembllist.get(0).getSeqId());
        }else{
            return null;
        }
    }

    @ApiOperation(value = "get whether ensembl exists by ensemblId", nickname = "getExistedEnsemblIdinAlignment")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Alignment.class, responseContainer = "boolean"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/EnsemblRecognitionQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public boolean getExistedEnsemblIdinAlignment(
            @RequestParam @ApiParam(value = "Input ensembl Id. For example ENSP00000489609.1", required = true, allowMultiple = true) String ensemblId) {
        List<Ensembl> ensembllist = ensemblRepository.findByEnsemblId(ensemblId);
        if(ensembllist.size()==1){
            return geneSequenceRepository.findBySeqId(ensembllist.get(0).getSeqId()).size() != 0;
        }else{
            return false;
        }
    }
        
    @ApiOperation(value = "get residue mapping from pdb alignments by ensemblId and Amino Acid Number", nickname = "getPdbResidueByEnsemblId")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Alignment.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/EnsemblResidueMappingQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<Residue> getPdbResidueByEnsemblId (
            @RequestParam(required = true)
            @ApiParam(value = "Input ensembl id. Example: ENSP00000489609.1", required = true, allowMultiple = true) String ensemblId, 
            @RequestParam(required = true)
            @ApiParam(value = "Input Amio Acid Number. Example: 300", required = true, allowMultiple = true) String aaNumber) {
        
        List<Ensembl> ensembllist = ensemblRepository.findByEnsemblId(ensemblId);
        
        if(ensembllist.size()==1){
            return getPdbResidueBySeqId(ensembllist.get(0).getSeqId(),aaNumber) ;
        }else{
            return null;
        }
    }
    
    //Query from UniprotIdIso
    @ApiOperation(value = "get pdb alignments by uniprotId and Isofrom", nickname = "getPdbAlignmentByUniprotIdIso")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Alignment.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/UniprotIsoStructureMappingQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<Alignment> getPdbAlignmentByUniprotIdIso (
            @RequestParam @ApiParam(value = "Input uniprot Id. For example Q26540", required = true, allowMultiple = true) String uniprotId, 
            @RequestParam(required = true)
            @ApiParam(value = "Input Isoform Number. Example: 1", required = true, allowMultiple = true) String isoform){
        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotIdIso(uniprotId+"_"+isoform); 
        if(uniprotlist.size()==1){
            return alignmentRepository.findBySeqId(uniprotlist.get(0).getSeqId());
        }else{
            return new ArrayList<Alignment>();
        }
    }

    @ApiOperation(value = "get whether Uniprot exists by uniprotId and Isofrom", nickname = "getExistedUniprotIdIsoinAlignment")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Alignment.class, responseContainer = "boolean"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/UniprotIsoRecognitionQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public boolean getExistedUniprotIdIsoinAlignment(
            @RequestParam @ApiParam(value = "Input uniprotId. For example Q26540", required = true, allowMultiple = true) String uniprotId, 
            @RequestParam(required = true)
            @ApiParam(value = "Input Isoform Number. Example: 1", required = true, allowMultiple = true) String isoform){
        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotIdIso(uniprotId+"_"+isoform);
        if(uniprotlist.size()==1){
            return geneSequenceRepository.findBySeqId(uniprotlist.get(0).getSeqId()).size() != 0;
        }else{
            return false;
        }
    }
        
    @ApiOperation(value = "get residue mapping from pdb alignments by uniprotId, Isofrom and Residue Number", nickname = "getPdbResidueByUniprotIdIso")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Alignment.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/UniprotIsoResidueMappingQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<Residue> getPdbResidueByUniprotIdIso (
            @RequestParam(required = true)
            @ApiParam(value = "Input uniprot Id. Example: Q26540", required = true, allowMultiple = true) String uniprotId, 
            @RequestParam(required = true)
            @ApiParam(value = "Input Isoform Number. Example: 1", required = true, allowMultiple = true) String isoform,
            @RequestParam(required = true)
            @ApiParam(value = "Input Amio Acid Number. Example: 12", required = true, allowMultiple = true) String aaNumber) {
        
        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotIdIso(uniprotId+"_"+isoform);       
        if(uniprotlist.size()==1){
            return getPdbResidueBySeqId(uniprotlist.get(0).getSeqId(),aaNumber) ;
        }else{
            return new ArrayList<Residue>();
        }
    }
    
    //Query from UniprotId
    @ApiOperation(value = "get pdb alignments by uniprotId", nickname = "getPdbAlignmentByUniprotId")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Alignment.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/UniprotStructureMappingQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<Alignment> getPdbAlignmentByUniprotId (
            @RequestParam @ApiParam(value = "Input uniprot Id. For example Q26540", required = true, allowMultiple = true) String uniprotId){
        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId); 
        ArrayList<Alignment> outList = new ArrayList<Alignment>();       
        for(Uniprot entry: uniprotList){
            outList.addAll(alignmentRepository.findBySeqId(entry.getSeqId()));
        }       
        return outList;       
    }

    @ApiOperation(value = "get whether Uniprot exists by uniprotId", nickname = "getExistedUniprotIdinAlignment")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Alignment.class, responseContainer = "boolean"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/UniprotRecognitionQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public boolean getExistedUniprotIdinAlignment(
            @RequestParam @ApiParam(value = "Input uniprotId. For example Q26540", required = true, allowMultiple = true) String uniprotId){
        
        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId); 
        ArrayList<Alignment> outList = new ArrayList<Alignment>();       
        for(Uniprot entry: uniprotList){
            outList.addAll(alignmentRepository.findBySeqId(entry.getSeqId()));
        }       
        return outList.size()!=0;
    }
        
    @ApiOperation(value = "get residue mapping from pdb alignments by uniprotId and Amino Acid Number", nickname = "getPdbResidueByUniprotId")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Alignment.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/UniprotResidueMappingQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<Residue> getPdbResidueByUniprotId (
            @RequestParam(required = true)
            @ApiParam(value = "Input uniprot Id. Example: Q26540", required = true, allowMultiple = true) String uniprotId, 
            @RequestParam(required = true)
            @ApiParam(value = "Input Amio Acid Number. Example: 12", required = true, allowMultiple = true) String aaNumber) {
        
        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId); 
        ArrayList<Residue> outList = new ArrayList<Residue>();       
        for(Uniprot entry: uniprotList){
            outList.addAll(getPdbResidueBySeqId(entry.getSeqId(),aaNumber));
        }       
        return outList;
    }
    
    //Genome to Structure:
    @ApiOperation(value = "get whether ensemblId exists by Genome", nickname = "getExistedEnsemblIdinGenome")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = GenomeResidue.class, responseContainer = "boolean"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/GenomeEnsemblRecognitionQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public String getExistedEnsemblIdinGenome(
            @RequestParam(required = true) 
            @ApiParam(value = "Input Chromomsome Number. For example X", required = true, allowMultiple = true) String chromosomeNum,
            @RequestParam(required = true)
            @ApiParam(value = "Input Position. Example: 66937331", required = true, allowMultiple = true) long positionNum,
            @RequestParam(required = true)
            @ApiParam(value = "Input Nucleotide of the Position. E.g. T Note: Please input correct nucleotide", required = true, allowMultiple = true) String nucleotideName) {
        
        //Calling GenomeNexus
        UtilAPI uapi = new UtilAPI();
        List<GenomeResidueInput> grlist = new ArrayList<GenomeResidueInput>();
        try{
            grlist = uapi.callAPI(chromosomeNum, positionNum, nucleotideName);
        }catch(HttpClientErrorException ex){
            ex.printStackTrace();
            //org.springframework.web.client.HttpClientErrorException: 400 Bad Request
            return "Input error, Please check";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        List<Ensembl> ensembllist = new ArrayList<Ensembl>();
        for(GenomeResidueInput gr: grlist){
            ensembllist.addAll(ensemblRepository.findByEnsemblIdStartingWith(gr.getEnsembl().getEnsemblid()));
        }
        if(ensembllist.size()>=1){
            for(Ensembl ensembl:ensembllist){
                if(geneSequenceRepository.findBySeqId(ensembl.getSeqId()).size() != 0){
                    return "true";
                }
            }
            return "false";
        }else{
            return "false";
        }
    }
        
    @ApiOperation(value = "get residue mapping from Genome", nickname = "getPdbResidueByGenome")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = GenomeResidue.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/GenomeEnsemblResidueMappingQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<GenomeResidue> getPdbResidueByEnsemblIdGenome (
            @RequestParam(required = true) 
            @ApiParam(value = "Input Chromomsome Number. For example X", required = true, allowMultiple = true) String chromosomeNum,
            @RequestParam(required = true)
            @ApiParam(value = "Input Position. Example: 66937331", required = true, allowMultiple = true) long positionNum,
            @RequestParam(required = true)
            @ApiParam(value = "Input Nucleotide of the Position. E.g. T Note: Please input correct nucleotide", required = true, allowMultiple = true) String nucleotideName) {
        
        //Calling GenomeNexus
        UtilAPI uapi = new UtilAPI();
        
        List<GenomeResidueInput> grlist = new ArrayList<GenomeResidueInput>();
        try{
            grlist = uapi.callAPI(chromosomeNum, positionNum, nucleotideName);
        }catch(HttpClientErrorException ex){
            ex.printStackTrace();
            //org.springframework.web.client.HttpClientErrorException: 400 Bad Request
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        List<GenomeResidueInput> grlistValid = new ArrayList<GenomeResidueInput>();
        for(GenomeResidueInput gr: grlist){
            List<Ensembl> ensembllist=ensemblRepository.findByEnsemblIdStartingWith(gr.getEnsembl().getEnsemblid());
            //System.out.println(gr.getEnsembl().getEnsemblid());
            
            if(geneSequenceRepository.findBySeqId(ensembllist.get(0).getSeqId()).size()!=0){
                Ensembl es = gr.getEnsembl();
                es.setSeqId(ensembllist.get(0).getSeqId());
                //System.out.println("API ensemblID:\t"+es.getEnsemblid()+"\t:"+es.getSeqId());
                gr.setEnsembl(es);
                grlistValid.add(gr);
            }
        }
        
        //System.out.println("grlistValid size is "+grlistValid.size());
        
        if(grlistValid.size()>=1){
            List<GenomeResidue> outlist = new ArrayList<GenomeResidue>(); 
            for(GenomeResidueInput gr:grlistValid){
                //System.out.println("Out:\t"+gr.getEnsembl().getSeqId()+"\t:"+Integer.toString(gr.getResidue().getResidueNum()));
                List<Residue> list= getPdbResidueBySeqId(gr.getEnsembl().getSeqId(),Integer.toString(gr.getResidue().getResidueNum()));
                Ensembl en = gr.getEnsembl();
                for(Residue re:list){
                    GenomeResidue ge = new GenomeResidue();
                    try{
                        BeanUtils.copyProperties(ge, re);
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }                  
                    ge.setEnsembl(en);
                    outlist.add(ge);
                }
            }
            
            return outlist ;
        }else{
            return null;
        }
    }
    
 
}
